#!/bin/bash
# Starts each backend microservice in its OWN terminal window, titled with
# the service name, so I can watch each one's logs separately.
#
# Make sure Postgres is running first, otherwise the services won't boot.
# macOS only (uses osascript). Auto-detects iTerm, otherwise uses Terminal.
#
#   ./run-all.sh start
#   ./run-all.sh stop
#   ./run-all.sh status

ROOT="$(cd "$(dirname "$0")" && pwd)"
MVN="${MVN:-mvn}"

# the project needs JDK 17 (Lombok breaks on newer JDKs)
if [ -d "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home" ]; then
  export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
fi

# service folder and the port it runs on.
# eureka comes first (registry), gateway last (front door).
services=(
  "eureka-server:8761"
  "users-service:8003"
  "auth-service:8004"
  "aadhar-service:8005"
  "company-service:8006"
  "payment-gateway-service:8008"
  "account-service:8001"
  "transactions-service:8002"
  "gateway-service:8080"
)

# returns the pid listening on a port, if there is one
pid_on_port() {
  lsof -ti "tcp:$1" 2>/dev/null
}

# opens a new terminal window, titles it, and runs the command inside
open_terminal() {
  local name="$1" dir="$2" cmd="$3"
  if [ "$TERM_PROGRAM" = "iTerm.app" ]; then
    osascript \
      -e "tell application \"iTerm\"" \
      -e "  set w to (create window with default profile)" \
      -e "  tell current session of w" \
      -e "    set name to \"$name\"" \
      -e "    write text \"cd '$dir' && $cmd\"" \
      -e "  end tell" \
      -e "end tell" >/dev/null
  else
    osascript \
      -e "tell application \"Terminal\"" \
      -e "  activate" \
      -e "  set t to do script \"cd '$dir' && $cmd\"" \
      -e "  set custom title of t to \"$name\"" \
      -e "end tell" >/dev/null
  fi
}

start() {
  echo "Opening a terminal window per service..."
  for s in "${services[@]}"; do
    name="${s%%:*}"
    port="${s##*:}"

    if [ -n "$(pid_on_port "$port")" ]; then
      echo "  $name is already running on $port, skipping"
      continue
    fi

    echo "  launching $name in its own window (port $port)"
    open_terminal "$name" "$ROOT/$name" "$MVN spring-boot:run"
    sleep 1
  done
  echo ""
  echo "Each service has its own window, titled with the service name."
}

stop() {
  echo "Stopping services..."
  for s in "${services[@]}"; do
    name="${s%%:*}"
    port="${s##*:}"
    pids="$(pid_on_port "$port")"

    if [ -n "$pids" ]; then
      for pid in $pids; do
        pkill -TERM -P "$pid" 2>/dev/null
        kill -TERM "$pid" 2>/dev/null
      done
      echo "  stopped $name ($port)"
    else
      echo "  $name ($port) wasn't running"
    fi
  done
  echo "(the terminal windows stay open so you can read the final logs)"
}

status() {
  for s in "${services[@]}"; do
    name="${s%%:*}"
    port="${s##*:}"
    if [ -n "$(pid_on_port "$port")" ]; then
      echo "  up    - $name ($port)"
    else
      echo "  down  - $name ($port)"
    fi
  done
}

case "$1" in
  start)   start ;;
  stop)    stop ;;
  restart) stop; sleep 2; start ;;
  status)  status ;;
  *)       echo "usage: $0 {start|stop|restart|status}" ;;
esac
