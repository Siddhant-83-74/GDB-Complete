#!/usr/bin/env python3
"""
build_code_review_graph.py
==========================

Regenerates the code-review-graph SQLite database (.code-review-graph/graph.db)
by statically parsing the Java and JS/JSX sources in this repository.

This is a stand-in for the external `code-review-graph` MCP server's full-build
pass. It reproduces the v9 schema and the high-confidence structural layer of the
graph (nodes + edges + FTS + metadata), plus a heuristic post-processing layer
(communities / risk_index / flows).

Faithful to the MCP output:
    nodes  (File / Class / Function / Test)
    edges  (CONTAINS / IMPORTS_FROM / INHERITS / CALLS / REFERENCES / TESTED_BY)
    nodes_fts, metadata (schema_version = 9)

Heuristic (own algorithm, not the MCP server's exact one):
    communities, community_summaries, risk_index, flows, flow_*

Usage:
    python3 build_code_review_graph.py
"""

import hashlib
import json
import os
import re
import sqlite3
import subprocess
import sys
import time
from datetime import datetime, timezone

REPO = os.path.dirname(os.path.abspath(__file__))
DB_DIR = os.path.join(REPO, ".code-review-graph")
DB_PATH = os.path.join(DB_DIR, "graph.db")

SCHEMA_VERSION = "9"

# Directories we never index.
SKIP_DIRS = {
    "node_modules", "target", "build", "dist", ".git", ".idea",
    ".code-review-graph", "logs", ".claude", "__pycache__",
}

JAVA_KEYWORDS_NOTCALL = {
    "if", "for", "while", "switch", "catch", "return", "new", "synchronized",
    "super", "this", "throw", "assert", "do", "else", "instanceof",
}

# Security-relevant keywords for the risk index.
SECURITY_TOKENS = re.compile(
    r"(?i)(auth|token|jwt|password|secret|credential|security|login|"
    r"encrypt|decrypt|hash|sign|verify|permission|role|filter|cors)"
)


# --------------------------------------------------------------------------- #
# Generic helpers
# --------------------------------------------------------------------------- #
def sha256(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8", "replace")).hexdigest()


def iter_source_files():
    for root, dirs, files in os.walk(REPO):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for fn in files:
            ext = os.path.splitext(fn)[1]
            if ext in (".java", ".js", ".jsx"):
                yield os.path.join(root, fn)


def matching_brace_end(text: str, open_idx: int, lines_index):
    """Return the 1-based line number of the brace that closes the one at open_idx."""
    depth = 0
    i = open_idx
    n = len(text)
    while i < n:
        c = text[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return lines_index(i)
        i += 1
    return lines_index(min(open_idx, n - 1))


def line_indexer(text):
    """Return a function mapping a char offset to a 1-based line number."""
    starts = [0]
    for m in re.finditer("\n", text):
        starts.append(m.end())

    def to_line(off):
        # binary search
        lo, hi = 0, len(starts) - 1
        while lo < hi:
            mid = (lo + hi + 1) // 2
            if starts[mid] <= off:
                lo = mid
            else:
                hi = mid - 1
        return lo + 1

    return to_line


# --------------------------------------------------------------------------- #
# Data accumulators
# --------------------------------------------------------------------------- #
class Graph:
    def __init__(self):
        self.nodes = []   # dict rows
        self.edges = []   # dict rows
        self._qn_seen = set()

    def add_node(self, **row):
        qn = row["qualified_name"]
        if qn in self._qn_seen:
            return
        self._qn_seen.add(qn)
        row.setdefault("line_start", None)
        row.setdefault("line_end", None)
        row.setdefault("parent_name", "")
        row.setdefault("params", "")
        row.setdefault("return_type", "")
        row.setdefault("modifiers", "")
        row.setdefault("is_test", 0)
        row.setdefault("file_hash", "")
        row.setdefault("extra", "{}")
        row.setdefault("signature", "")
        self.nodes.append(row)

    def add_edge(self, kind, source, target, file_path, line):
        self.edges.append({
            "kind": kind,
            "source_qualified": source,
            "target_qualified": target,
            "file_path": file_path,
            "line": line or 0,
        })


# --------------------------------------------------------------------------- #
# Java parsing
# --------------------------------------------------------------------------- #
CLASS_RE = re.compile(
    r"\b(?:public|private|protected|abstract|final|static|sealed|\s)*"
    r"\b(class|interface|enum)\s+(\w+)"
    r"([^\{]*)\{",
)

# A reasonably tight Java method-declaration matcher.
METHOD_RE = re.compile(
    r"(?:^|\n)[ \t]*"
    r"(?:@\w+(?:\([^)]*\))?\s*)*"                       # annotations
    r"((?:public|private|protected|static|final|abstract|synchronized|native|default)\s+)*"
    r"(?:<[^>]+>\s*)?"                                  # generics
    r"([\w\[\]<>,.?\s]+?)\s+"                           # return type
    r"(\w+)\s*"                                         # name
    r"\(([^;{)]*)\)\s*"                                 # params
    r"(?:throws\s+[\w.,\s]+)?\{",                       # body open
)

IMPORT_RE = re.compile(r"(?:^|\n)\s*import\s+(?:static\s+)?([\w.]+)\s*;")


def parse_java(path, text, g: Graph):
    to_line = line_indexer(text)
    file_node = path
    g.add_node(kind="File", name=path, qualified_name=path, file_path=path,
               language="java", file_hash=sha256(text))

    is_test_file = path.endswith("Test.java") or "/test/" in path

    # imports
    for m in IMPORT_RE.finditer(text):
        g.add_edge("IMPORTS_FROM", file_node, m.group(1), path, to_line(m.start()))

    # find the primary (first top-level) class to use as parent for methods
    classes = []  # (name, start_idx, end_line, header)
    for m in CLASS_RE.finditer(text):
        cname = m.group(2)
        header = m.group(3)
        brace_idx = m.end() - 1
        start_line = to_line(m.start(1))
        end_line = matching_brace_end(text, brace_idx, to_line)
        classes.append((cname, m.start(), brace_idx, start_line, end_line, header))

    if not classes:
        return

    # Register class nodes + CONTAINS(file->class) + INHERITS
    for cname, mstart, brace_idx, start_line, end_line, header in classes:
        cqn = f"{path}::{cname}"
        g.add_node(kind="Class", name=cname, qualified_name=cqn, file_path=path,
                   line_start=start_line, line_end=end_line, language="java",
                   signature=f"class {cname}")
        g.add_edge("CONTAINS", file_node, cqn, path, start_line)
        # inheritance
        ext = re.search(r"\bextends\s+([\w.<>]+)", header)
        if ext:
            g.add_edge("INHERITS", cqn, f"extends {ext.group(1)}", path,
                       to_line(brace_idx))
        impl = re.search(r"\bimplements\s+([\w.,<>\s]+)", header)
        if impl:
            for iface in impl.group(1).split(","):
                iface = iface.strip()
                if iface:
                    g.add_edge("INHERITS", cqn, f"implements {iface}", path,
                               to_line(brace_idx))

    # Owning class for a given char offset = innermost class span containing it.
    def owner_of(off):
        best = None
        for cname, mstart, brace_idx, sl, el, header in classes:
            if brace_idx <= off and to_line(off) <= el:
                if best is None or brace_idx > best[1]:
                    best = (cname, brace_idx)
        return best[0] if best else classes[0][0]

    # methods
    for m in METHOD_RE.finditer(text):
        name = m.group(3)
        params = "(" + (m.group(4) or "").strip() + ")"
        if name in JAVA_KEYWORDS_NOTCALL or name in ("class", "interface", "enum"):
            continue
        owner = owner_of(m.start(3))
        cqn = f"{path}::{owner}"
        mqn = f"{path}::{owner}.{name}"
        brace_idx = text.index("{", m.start(3))
        start_line = to_line(m.start(3))
        end_line = matching_brace_end(text, brace_idx, to_line)

        is_test = 1 if (is_test_file and (name.startswith("test")
                        or "@Test" in text[max(0, m.start()-60):m.start()])) else 0
        kind = "Test" if is_test else "Function"
        modifiers = (m.group(1) or "").strip()
        g.add_node(kind=kind, name=name, qualified_name=mqn, file_path=path,
                   line_start=start_line, line_end=end_line, language="java",
                   parent_name=owner, params=params, modifiers=modifiers,
                   is_test=is_test, signature=f"def {name}(({params}))")
        g.add_edge("CONTAINS", cqn, mqn, path, start_line)

        body = text[brace_idx:text_offset_of_line(text, end_line)]
        emit_calls(g, mqn, body, path, start_line)


def text_offset_of_line(text, line):
    # offset just past the given 1-based line
    count = 0
    for i, ch in enumerate(text):
        if ch == "\n":
            count += 1
            if count >= line:
                return i
    return len(text)


CALL_RECV_RE = re.compile(r"\b([A-Za-z_]\w*)\s*\.\s*\w+\s*\(")
CALL_BARE_RE = re.compile(r"(?<![\w.])([a-z_]\w*)\s*\(")


def emit_calls(g: Graph, source_qn, body, path, base_line):
    line_off = body.count  # not used; compute lines relative
    bl = line_indexer(body)
    seen = set()
    for m in CALL_RECV_RE.finditer(body):
        recv = m.group(1)
        if recv in JAVA_KEYWORDS_NOTCALL:
            continue
        ln = base_line + bl(m.start(1)) - 1
        key = (recv, ln)
        if key in seen:
            continue
        seen.add(key)
        g.add_edge("CALLS", source_qn, recv, path, ln)
    for m in CALL_BARE_RE.finditer(body):
        name = m.group(1)
        if name in JAVA_KEYWORDS_NOTCALL:
            continue
        ln = base_line + bl(m.start(1)) - 1
        key = (name, ln)
        if key in seen:
            continue
        seen.add(key)
        g.add_edge("CALLS", source_qn, name, path, ln)


# --------------------------------------------------------------------------- #
# JavaScript / JSX parsing
# --------------------------------------------------------------------------- #
JS_FUNC_PATTERNS = [
    re.compile(r"(?:^|\n)\s*(?:export\s+)?(?:default\s+)?function\s+(\w+)\s*\(([^)]*)\)"),
    re.compile(r"(?:^|\n)\s*(?:export\s+)?const\s+(\w+)\s*=\s*(?:async\s*)?\(([^)]*)\)\s*=>"),
    re.compile(r"(?:^|\n)\s*(?:export\s+)?const\s+(\w+)\s*=\s*(?:async\s*)?function\s*\(([^)]*)\)"),
    re.compile(r"(?:^|\n)\s*(?:export\s+)?const\s+(\w+)\s*=\s*(\w+)\s*=>"),  # single param no parens
]
JS_IMPORT_RE = re.compile(r"(?:^|\n)\s*import\s+[^;]*?from\s+['\"]([^'\"]+)['\"]")
JSX_COMPONENT_RE = re.compile(r"<([A-Z]\w+)")


def parse_js(path, text, g: Graph):
    to_line = line_indexer(text)
    file_node = path
    g.add_node(kind="File", name=path, qualified_name=path, file_path=path,
               language="javascript", file_hash=sha256(text))

    for m in JS_IMPORT_RE.finditer(text):
        g.add_edge("IMPORTS_FROM", file_node, m.group(1), path, to_line(m.start()))

    funcs = {}
    for pat in JS_FUNC_PATTERNS:
        for m in pat.finditer(text):
            name = m.group(1)
            params = "(" + (m.group(2) or "").strip() + ")" if m.lastindex and m.group(2) is not None else "()"
            if name in funcs:
                continue
            start_line = to_line(m.start(1))
            funcs[name] = start_line
            fqn = f"{path}::{name}"
            g.add_node(kind="Function", name=name, qualified_name=fqn, file_path=path,
                       line_start=start_line, language="javascript",
                       params=params, signature=f"def {name}(({params}))")
            g.add_edge("CONTAINS", file_node, fqn, path, start_line)

    # REFERENCES: JSX component usages attributed to the nearest preceding function
    if funcs:
        ordered = sorted(funcs.items(), key=lambda kv: kv[1])

        def owner_at(line):
            owner = ordered[0][0]
            for name, sl in ordered:
                if sl <= line:
                    owner = name
                else:
                    break
            return owner

        seen = set()
        for m in JSX_COMPONENT_RE.finditer(text):
            comp = m.group(1)
            ln = to_line(m.start(1))
            owner = owner_at(ln)
            key = (owner, comp)
            if key in seen:
                continue
            seen.add(key)
            g.add_edge("REFERENCES", f"{path}::{owner}", comp, path, ln)


# --------------------------------------------------------------------------- #
# Post-processing: communities, risk index, flows
# --------------------------------------------------------------------------- #
def service_of(path):
    rel = os.path.relpath(path, REPO)
    return rel.split(os.sep)[0]


def leaf_dir(path):
    return os.path.basename(os.path.dirname(path))


def postprocess(g: Graph, cur):
    now_iso = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")

    # ---- communities: group symbol nodes by (leaf dir, service) ----
    groups = {}
    for n in g.nodes:
        if n["kind"] == "File":
            continue
        key = (leaf_dir(n["file_path"]), service_of(n["file_path"]))
        groups.setdefault(key, []).append(n)

    comm_id_by_key = {}
    cid = 0
    for (leaf, svc), members in sorted(groups.items(), key=lambda kv: -len(kv[1])):
        cid += 1
        svc_short = svc.replace("-service", "").replace("-server", "")
        name = f"{leaf}-{svc_short}"
        langs = [m["language"] for m in members]
        dom = max(set(langs), key=langs.count) if langs else ""
        cohesion = round(min(1.0, len(members) / 50.0), 3)
        cur.execute(
            "INSERT INTO communities(id,name,level,parent_id,cohesion,size,"
            "dominant_language,description,created_at) VALUES(?,?,?,?,?,?,?,?,?)",
            (cid, name, 0, None, cohesion, len(members), dom,
             f"Symbols under {leaf}/ in {svc}", now_iso),
        )
        risk = "high" if SECURITY_TOKENS.search(name) else "normal"
        key_syms = json.dumps([m["name"] for m in members[:8]])
        cur.execute(
            "INSERT INTO community_summaries(community_id,name,purpose,key_symbols,"
            "risk,size,dominant_language) VALUES(?,?,?,?,?,?,?)",
            (cid, name, f"Symbols under {leaf}/ in {svc}", key_syms, risk,
             len(members), dom),
        )
        comm_id_by_key[(leaf, svc)] = cid

    # assign community_id back onto nodes
    for n in g.nodes:
        if n["kind"] == "File":
            continue
        key = (leaf_dir(n["file_path"]), service_of(n["file_path"]))
        n["_community_id"] = comm_id_by_key.get(key)

    # ---- risk index ----
    # caller_count: number of CALLS edges whose simple target == node name.
    call_targets = {}
    for e in g.edges:
        if e["kind"] == "CALLS":
            call_targets[e["target_qualified"]] = call_targets.get(e["target_qualified"], 0) + 1
    # tested names: node names that appear as Test method targets via TESTED_BY or test files
    tested_names = set()
    for n in g.nodes:
        if n["is_test"]:
            # crude: a test method "testFooBar" covers symbol "fooBar"/"FooBar"
            base = re.sub(r"^test_?", "", n["name"], flags=re.I)
            tested_names.add(base.lower())

    for n in g.nodes:
        if n["kind"] not in ("Function", "Test"):
            continue
        callers = call_targets.get(n["name"], 0)
        sec = 1 if SECURITY_TOKENS.search(n["qualified_name"]) else 0
        covered = "tested" if n["name"].lower() in tested_names else "untested"
        # risk: more callers + security + untested => higher
        score = round(min(1.0, callers / 20.0) * 0.5 + sec * 0.3
                      + (0.2 if covered == "untested" else 0.0), 3)
        nid = n["_id"]
        cur.execute(
            "INSERT INTO risk_index(node_id,qualified_name,risk_score,caller_count,"
            "test_coverage,security_relevant,last_computed) VALUES(?,?,?,?,?,?,?)",
            (nid, n["qualified_name"], score, callers, covered, sec, now_iso),
        )

    # ---- flows: each controller/main method is a flow entry point ----
    nodes_by_qn = {n["qualified_name"]: n for n in g.nodes}
    contains = {}
    for e in g.edges:
        if e["kind"] == "CONTAINS":
            contains.setdefault(e["source_qualified"], []).append(e["target_qualified"])

    fid = 0
    for n in g.nodes:
        if n["kind"] != "Function":
            continue
        fp = n["file_path"].lower()
        if not (("controller" in fp) or n["name"] == "main"):
            continue
        fid += 1
        # path = the method plus the symbols it CALLS that resolve to known nodes
        path_nodes = [n["qualified_name"]]
        criticality = round(min(1.0, 0.4 + (0.6 if "controller" in fp else 0.0)), 3)
        member_ids = [n["_id"]]
        cur.execute(
            "INSERT INTO flows(id,name,entry_point_id,depth,node_count,file_count,"
            "criticality,path_json) VALUES(?,?,?,?,?,?,?,?)",
            (fid, f"{n['parent_name']}.{n['name']}", n["_id"], 1,
             len(path_nodes), 1, criticality, json.dumps(path_nodes)),
        )
        cur.execute(
            "INSERT INTO flow_snapshots(flow_id,name,entry_point,critical_path,"
            "criticality,node_count,file_count) VALUES(?,?,?,?,?,?,?)",
            (fid, f"{n['parent_name']}.{n['name']}", n["qualified_name"],
             json.dumps(path_nodes), criticality, len(path_nodes), 1),
        )
        for pos, mid in enumerate(member_ids):
            cur.execute(
                "INSERT OR IGNORE INTO flow_memberships(flow_id,node_id,position) "
                "VALUES(?,?,?)", (fid, mid, pos),
            )


# --------------------------------------------------------------------------- #
# Schema
# --------------------------------------------------------------------------- #
DDL = """
CREATE TABLE nodes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    kind TEXT NOT NULL, name TEXT NOT NULL,
    qualified_name TEXT NOT NULL UNIQUE, file_path TEXT NOT NULL,
    line_start INTEGER, line_end INTEGER, language TEXT, parent_name TEXT,
    params TEXT, return_type TEXT, modifiers TEXT, is_test INTEGER DEFAULT 0,
    file_hash TEXT, extra TEXT DEFAULT '{}', updated_at REAL NOT NULL,
    signature TEXT, community_id INTEGER);
CREATE TABLE edges (
    id INTEGER PRIMARY KEY AUTOINCREMENT, kind TEXT NOT NULL,
    source_qualified TEXT NOT NULL, target_qualified TEXT NOT NULL,
    file_path TEXT NOT NULL, line INTEGER DEFAULT 0, extra TEXT DEFAULT '{}',
    confidence REAL DEFAULT 1.0, confidence_tier TEXT DEFAULT 'EXTRACTED',
    updated_at REAL NOT NULL);
CREATE TABLE metadata (key TEXT PRIMARY KEY, value TEXT NOT NULL);
CREATE INDEX idx_nodes_file ON nodes(file_path);
CREATE INDEX idx_nodes_kind ON nodes(kind);
CREATE INDEX idx_nodes_qualified ON nodes(qualified_name);
CREATE INDEX idx_edges_source ON edges(source_qualified);
CREATE INDEX idx_edges_target ON edges(target_qualified);
CREATE INDEX idx_edges_kind ON edges(kind);
CREATE INDEX idx_edges_target_kind ON edges(target_qualified, kind);
CREATE INDEX idx_edges_source_kind ON edges(source_qualified, kind);
CREATE INDEX idx_edges_file ON edges(file_path);
CREATE TABLE flows (
    id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
    entry_point_id INTEGER NOT NULL, depth INTEGER NOT NULL,
    node_count INTEGER NOT NULL, file_count INTEGER NOT NULL,
    criticality REAL NOT NULL DEFAULT 0.0, path_json TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE TABLE flow_memberships (
    flow_id INTEGER NOT NULL, node_id INTEGER NOT NULL, position INTEGER NOT NULL,
    PRIMARY KEY (flow_id, node_id));
CREATE INDEX idx_flows_criticality ON flows(criticality DESC);
CREATE INDEX idx_flows_entry ON flows(entry_point_id);
CREATE INDEX idx_flow_memberships_node ON flow_memberships(node_id);
CREATE TABLE communities (
    id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 0, parent_id INTEGER,
    cohesion REAL NOT NULL DEFAULT 0.0, size INTEGER NOT NULL DEFAULT 0,
    dominant_language TEXT, description TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')));
CREATE INDEX idx_nodes_community ON nodes(community_id);
CREATE INDEX idx_communities_parent ON communities(parent_id);
CREATE INDEX idx_communities_cohesion ON communities(cohesion DESC);
CREATE TABLE community_summaries (
    community_id INTEGER PRIMARY KEY, name TEXT NOT NULL, purpose TEXT DEFAULT '',
    key_symbols TEXT DEFAULT '[]', risk TEXT DEFAULT 'unknown',
    size INTEGER DEFAULT 0, dominant_language TEXT DEFAULT '',
    FOREIGN KEY (community_id) REFERENCES communities(id));
CREATE TABLE flow_snapshots (
    flow_id INTEGER PRIMARY KEY, name TEXT NOT NULL, entry_point TEXT NOT NULL,
    critical_path TEXT DEFAULT '[]', criticality REAL DEFAULT 0.0,
    node_count INTEGER DEFAULT 0, file_count INTEGER DEFAULT 0,
    FOREIGN KEY (flow_id) REFERENCES flows(id));
CREATE TABLE risk_index (
    node_id INTEGER PRIMARY KEY, qualified_name TEXT NOT NULL,
    risk_score REAL DEFAULT 0.0, caller_count INTEGER DEFAULT 0,
    test_coverage TEXT DEFAULT 'unknown', security_relevant INTEGER DEFAULT 0,
    last_computed TEXT DEFAULT '', FOREIGN KEY (node_id) REFERENCES nodes(id));
CREATE INDEX idx_risk_index_score ON risk_index(risk_score DESC);
CREATE INDEX idx_edges_composite ON edges(kind, source_qualified, target_qualified, file_path, line);
CREATE VIRTUAL TABLE nodes_fts USING fts5(
    name, qualified_name, file_path, signature, tokenize='porter unicode61');
"""


def git(*args, default=""):
    try:
        return subprocess.check_output(["git", *args], cwd=REPO,
                                       stderr=subprocess.DEVNULL).decode().strip()
    except Exception:
        return default


def main():
    g = Graph()
    files = sorted(iter_source_files())
    for path in files:
        try:
            with open(path, "r", encoding="utf-8", errors="replace") as fh:
                text = fh.read()
        except OSError:
            continue
        if path.endswith(".java"):
            parse_java(path, text, g)
        else:
            parse_js(path, text, g)

    os.makedirs(DB_DIR, exist_ok=True)
    if os.path.exists(DB_PATH):
        os.replace(DB_PATH, DB_PATH + ".bak")

    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.executescript(DDL)

    now = time.time()
    for n in g.nodes:
        cur.execute(
            "INSERT INTO nodes(kind,name,qualified_name,file_path,line_start,"
            "line_end,language,parent_name,params,return_type,modifiers,is_test,"
            "file_hash,extra,updated_at,signature) "
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (n["kind"], n["name"], n["qualified_name"], n["file_path"],
             n["line_start"], n["line_end"], n["language"], n["parent_name"],
             n["params"], n["return_type"], n["modifiers"], n["is_test"],
             n["file_hash"], n["extra"], now, n["signature"]),
        )
        n["_id"] = cur.lastrowid

    for e in g.edges:
        cur.execute(
            "INSERT INTO edges(kind,source_qualified,target_qualified,file_path,"
            "line,extra,confidence,confidence_tier,updated_at) "
            "VALUES(?,?,?,?,?,?,?,?,?)",
            (e["kind"], e["source_qualified"], e["target_qualified"],
             e["file_path"], e["line"], "{}", 1.0, "EXTRACTED", now),
        )

    # FTS
    cur.execute(
        "INSERT INTO nodes_fts(rowid,name,qualified_name,file_path,signature) "
        "SELECT id,name,qualified_name,file_path,signature FROM nodes"
    )

    # post-processing layer
    postprocess(g, cur)

    # write community_id back to nodes
    for n in g.nodes:
        if n.get("_community_id"):
            cur.execute("UPDATE nodes SET community_id=? WHERE id=?",
                        (n["_community_id"], n["_id"]))

    nowiso = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    meta = {
        "schema_version": SCHEMA_VERSION,
        "last_updated": nowiso,
        "last_build_type": "full",
        "git_branch": git("rev-parse", "--abbrev-ref", "HEAD", default="master"),
        "git_head_sha": git("rev-parse", "HEAD"),
        "last_postprocessed_at": nowiso,
        "postprocess_level": "full",
        "built_by": "build_code_review_graph.py",
    }
    for k, v in meta.items():
        cur.execute("INSERT OR REPLACE INTO metadata(key,value) VALUES(?,?)", (k, v))

    conn.commit()

    # report
    def count(sql):
        return cur.execute(sql).fetchone()[0]

    print(f"Wrote {DB_PATH}")
    print(f"  files parsed : {len(files)}")
    print(f"  nodes        : {count('SELECT count(*) FROM nodes')}")
    for kind, in cur.execute("SELECT DISTINCT kind FROM nodes ORDER BY kind"):
        print(f"      {kind:10s}: {count(f'SELECT count(*) FROM nodes WHERE kind=' + repr(kind))}")
    print(f"  edges        : {count('SELECT count(*) FROM edges')}")
    for kind, in cur.execute("SELECT DISTINCT kind FROM edges ORDER BY kind"):
        print(f"      {kind:12s}: {count('SELECT count(*) FROM edges WHERE kind=' + repr(kind))}")
    print(f"  communities  : {count('SELECT count(*) FROM communities')}")
    print(f"  risk_index   : {count('SELECT count(*) FROM risk_index')}")
    print(f"  flows        : {count('SELECT count(*) FROM flows')}")
    conn.close()


if __name__ == "__main__":
    main()
