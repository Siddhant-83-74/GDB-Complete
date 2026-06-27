import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/creditCardsService';
import {
  ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend, AreaChart, Area,
} from 'recharts';
import {
  ArrowLeft, CreditCard, TrendingUp, Wallet, AlertTriangle, IndianRupee, RefreshCw, Search,
} from 'lucide-react';
import toast from 'react-hot-toast';

const PALETTE = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6'];
const CATEGORY_COLORS = { SILVER: '#94a3b8', GOLD: '#f59e0b', PLATINUM: '#475569' };

const inr = (n) => `₹${Number(n || 0).toLocaleString('en-IN')}`;
const compact = (n) => {
  const v = Number(n || 0);
  if (v >= 1e7) return `₹${(v / 1e7).toFixed(2)} Cr`;
  if (v >= 1e5) return `₹${(v / 1e5).toFixed(2)} L`;
  if (v >= 1e3) return `₹${(v / 1e3).toFixed(1)}K`;
  return `₹${v}`;
};
const monthLabel = (ym) => {
  const [y, m] = (ym || '').split('-');
  const d = new Date(Number(y), Number(m) - 1, 1);
  return d.toLocaleDateString('en-IN', { month: 'short', year: '2-digit' });
};

const KpiCard = ({ icon: Icon, label, value, sub, accent }) => (
  <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
    <div className="flex items-center justify-between">
      <p className="text-sm font-medium text-gray-500">{label}</p>
      <div className={`w-9 h-9 rounded-lg flex items-center justify-center ${accent}`}>
        <Icon className="w-5 h-5" />
      </div>
    </div>
    <p className="text-2xl font-bold text-gray-900 mt-3">{value}</p>
    {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
  </div>
);

const ChartCard = ({ title, children, className = '' }) => (
  <div className={`bg-white rounded-xl shadow-sm border border-gray-100 p-6 ${className}`}>
    <h3 className="font-semibold text-gray-800 mb-4">{title}</h3>
    {children}
  </div>
);

// Reusable table pager (windowed numbered buttons + prev/next)
const Pager = ({ page, totalPages, onChange, label }) => {
  if (totalPages <= 1) return null;
  const windowSize = Math.min(5, totalPages);
  let start;
  if (totalPages <= 5) start = 1;
  else if (page <= 3) start = 1;
  else if (page >= totalPages - 2) start = totalPages - 4;
  else start = page - 2;
  const pages = Array.from({ length: windowSize }, (_, i) => start + i);
  return (
    <div className="px-6 py-3 border-t border-gray-100 bg-gray-50 flex flex-col sm:flex-row items-center justify-between gap-3">
      <span className="text-xs text-gray-500">{label}</span>
      <div className="flex items-center gap-1.5">
        <button onClick={() => onChange(page - 1)} disabled={page === 1}
          className="px-2.5 py-1 border border-gray-300 rounded-md text-sm text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">Prev</button>
        {pages.map((p) => (
          <button key={p} onClick={() => onChange(p)}
            className={`px-3 py-1 rounded-md text-sm border ${p === page ? 'bg-primary-600 text-white border-primary-600' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'}`}>{p}</button>
        ))}
        <button onClick={() => onChange(page + 1)} disabled={page === totalPages}
          className="px-2.5 py-1 border border-gray-300 rounded-md text-sm text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">Next</button>
      </div>
    </div>
  );
};

const CreditCardAnalytics = () => {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [allCards, setAllCards] = useState([]);
  const [cardQuery, setCardQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [topPage, setTopPage] = useState(1);
  const [allPage, setAllPage] = useState(1);
  const TOP_PAGE_SIZE = 6;
  const ALL_PAGE_SIZE = 8;

  // Reset the All Cards table to page 1 whenever the search changes
  useEffect(() => { setAllPage(1); }, [cardQuery]);

  const load = async () => {
    try {
      setLoading(true);
      const [res, cards] = await Promise.all([
        creditCardService.getPortfolioAnalytics(),
        creditCardService.getAllCardsPortfolio(),
      ]);
      setData(res);
      setAllCards(cards);
    } catch (e) {
      toast.error(e.message || 'Unable to load analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }
  if (!data) return null;

  const s = data.summary || {};
  const num = (arr) => (arr || []).map((d) => ({ ...d, value: Number(d.value) }));
  const cardsByCategory = num(data.cards_by_category);
  const cardsByStatus = num(data.cards_by_status);
  const spendByChannel = num(data.spend_by_channel);
  const spendByCategory = num(data.spend_by_category);
  const monthly = (data.monthly_spend || []).map((m) => ({
    month: monthLabel(m.month),
    purchases: Number(m.purchases),
    payments: Number(m.payments),
  }));
  const topCards = data.top_cards_by_utilization || [];
  const utilization = Number(s.overall_utilization || 0);

  // Filter (All Cards search) + paginate both report tables
  const filteredCards = allCards.filter((c) => {
    const q = cardQuery.trim().toLowerCase();
    if (!q) return true;
    return (
      (c.cardHolderName || '').toLowerCase().includes(q) ||
      (c.cardNumber || '').toLowerCase().includes(q) ||
      (c.vendor || '').toLowerCase().includes(q) ||
      (c.cardType || '').toLowerCase().includes(q) ||
      (c.status || '').toLowerCase().includes(q)
    );
  });

  const topTotalPages = Math.max(1, Math.ceil(topCards.length / TOP_PAGE_SIZE));
  const topPageSafe = Math.min(topPage, topTotalPages);
  const pagedTopCards = topCards.slice((topPageSafe - 1) * TOP_PAGE_SIZE, topPageSafe * TOP_PAGE_SIZE);

  const allTotalPages = Math.max(1, Math.ceil(filteredCards.length / ALL_PAGE_SIZE));
  const allPageSafe = Math.min(allPage, allTotalPages);
  const pagedAllCards = filteredCards.slice((allPageSafe - 1) * ALL_PAGE_SIZE, allPageSafe * ALL_PAGE_SIZE);

  return (
    <div className="max-w-7xl mx-auto space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/credit-cards')} className="p-2.5 bg-white shadow-sm hover:bg-gray-50 rounded-full border border-gray-100">
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Analytics Command Center</h1>
            <p className="text-gray-500">Portfolio-wide credit card intelligence across all active cards</p>
          </div>
        </div>
        <button onClick={load} className="btn-outline flex items-center gap-2">
          <RefreshCw className="w-4 h-4" /> Refresh
        </button>
      </div>

      {/* KPI row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard icon={CreditCard} label="Cards in Portfolio" value={s.total_cards ?? 0}
          sub={`${s.active_cards ?? 0} active · ${s.blocked_cards ?? 0} blocked`}
          accent="bg-indigo-100 text-indigo-600" />
        <KpiCard icon={Wallet} label="Total Credit Limit" value={compact(s.total_credit_limit)}
          sub={`${inr(s.total_available)} available`} accent="bg-emerald-100 text-emerald-600" />
        <KpiCard icon={TrendingUp} label="Total Outstanding" value={compact(s.total_outstanding)}
          sub={`Utilization ${utilization}%`} accent="bg-amber-100 text-amber-600" />
        <KpiCard icon={IndianRupee} label="Est. Fee Revenue" value={compact(s.fee_revenue)}
          sub="2.5% of outstanding (modelled)" accent="bg-pink-100 text-pink-600" />
      </div>

      {/* Utilization meter */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div className="flex justify-between items-center mb-2">
          <h3 className="font-semibold text-gray-800">Portfolio Utilization</h3>
          <span className="text-sm font-semibold text-gray-700">{utilization}%</span>
        </div>
        <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
          <div className={`h-full rounded-full transition-all ${utilization > 70 ? 'bg-red-500' : utilization > 40 ? 'bg-amber-500' : 'bg-emerald-500'}`}
            style={{ width: `${Math.min(utilization, 100)}%` }} />
        </div>
        <p className="text-xs text-gray-500 mt-2">{inr(s.total_outstanding)} drawn against {inr(s.total_credit_limit)} sanctioned</p>
      </div>

      {/* Charts grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Monthly spend trend */}
        <ChartCard title="Purchases vs Payments (6 months)" className="lg:col-span-2">
          <ResponsiveContainer width="100%" height={280}>
            <AreaChart data={monthly} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="gPurchase" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="gPayment" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tickFormatter={compact} tick={{ fontSize: 12 }} width={70} />
              <Tooltip formatter={(v) => inr(v)} />
              <Legend />
              <Area type="monotone" dataKey="purchases" stroke="#6366f1" fill="url(#gPurchase)" strokeWidth={2} name="Purchases" />
              <Area type="monotone" dataKey="payments" stroke="#10b981" fill="url(#gPayment)" strokeWidth={2} name="Payments" />
            </AreaChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Cards by category */}
        <ChartCard title="Cards by Category">
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie data={cardsByCategory} dataKey="value" nameKey="name" cx="50%" cy="50%"
                innerRadius={55} outerRadius={90} paddingAngle={3}>
                {cardsByCategory.map((entry, i) => (
                  <Cell key={i} fill={CATEGORY_COLORS[entry.name] || PALETTE[i % PALETTE.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(v, n) => [`${v} cards`, n]} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Spend by category */}
        <ChartCard title="Spend by Card Category">
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={spendByCategory} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tickFormatter={compact} tick={{ fontSize: 12 }} width={70} />
              <Tooltip formatter={(v) => inr(v)} />
              <Bar dataKey="value" radius={[6, 6, 0, 0]} name="Spend">
                {spendByCategory.map((entry, i) => (
                  <Cell key={i} fill={CATEGORY_COLORS[entry.name] || PALETTE[i % PALETTE.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Spend by channel */}
        <ChartCard title="Spend by Channel">
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={spendByChannel} layout="vertical" margin={{ top: 5, right: 20, left: 10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis type="number" tickFormatter={compact} tick={{ fontSize: 12 }} />
              <YAxis type="category" dataKey="name" tick={{ fontSize: 12 }} width={90} />
              <Tooltip formatter={(v) => inr(v)} />
              <Bar dataKey="value" radius={[0, 6, 6, 0]} fill="#8b5cf6" name="Spend" />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>

        {/* Cards by status */}
        <ChartCard title="Cards by Status">
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={cardsByStatus} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                {cardsByStatus.map((entry, i) => (
                  <Cell key={i} fill={entry.name === 'ACTIVE' ? '#10b981' : entry.name === 'BLOCKED' ? '#ef4444' : '#94a3b8'} />
                ))}
              </Pie>
              <Tooltip formatter={(v, n) => [`${v} cards`, n]} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      {/* Top cards by utilization */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-amber-500" />
          <h3 className="font-semibold text-gray-800">Highest Utilization Cards</h3>
          <span className="text-xs text-gray-400">— priority for credit review</span>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                {['Card Holder', 'Card', 'Category', 'Limit', 'Outstanding', 'Utilization'].map((h) => (
                  <th key={h} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {pagedTopCards.map((c) => {
                const u = Number(c.utilization);
                return (
                  <tr key={c.id} className="hover:bg-gray-50">
                    <td className="px-6 py-3 text-sm font-medium text-gray-900">{c.holder}</td>
                    <td className="px-6 py-3 text-sm font-mono text-gray-600">••{c.last4}</td>
                    <td className="px-6 py-3">
                      <span className="text-xs font-semibold px-2 py-0.5 rounded-full"
                        style={{ background: `${CATEGORY_COLORS[c.category] || '#94a3b8'}22`, color: CATEGORY_COLORS[c.category] || '#475569' }}>
                        {c.category}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-sm text-gray-700">{inr(c.credit_limit)}</td>
                    <td className="px-6 py-3 text-sm text-gray-700">{inr(c.outstanding)}</td>
                    <td className="px-6 py-3">
                      <div className="flex items-center gap-2 min-w-[140px]">
                        <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                          <div className={`h-full rounded-full ${u > 70 ? 'bg-red-500' : u > 40 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                            style={{ width: `${Math.min(u, 100)}%` }} />
                        </div>
                        <span className="text-xs font-semibold text-gray-700 w-10 text-right">{u}%</span>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {topCards.length === 0 && (
                <tr><td colSpan="6" className="px-6 py-10 text-center text-gray-500 text-sm">No card data available</td></tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager
          page={topPageSafe}
          totalPages={topTotalPages}
          onChange={setTopPage}
          label={`Showing ${(topPageSafe - 1) * TOP_PAGE_SIZE + 1}-${Math.min(topPageSafe * TOP_PAGE_SIZE, topCards.length)} of ${topCards.length}`}
        />
      </div>

      {/* Full portfolio — every card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <CreditCard className="w-4 h-4 text-indigo-500" />
            <h3 className="font-semibold text-gray-800">All Cards</h3>
            <span className="text-xs text-gray-400">— complete portfolio ({allCards.length})</span>
          </div>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={cardQuery}
              onChange={(e) => setCardQuery(e.target.value)}
              placeholder="Search holder, card, vendor…"
              className="pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 w-full sm:w-64"
            />
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                {['Card Holder', 'Card', 'Category', 'Vendor', 'Status', 'Limit', 'Available', 'Outstanding', 'Utilization', 'Mobile'].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {pagedAllCards.map((c) => {
                  const u = c.creditLimit > 0 ? Math.round((c.outstandingAmount / c.creditLimit) * 100) : 0;
                  const statusColor = c.status === 'Active' ? 'bg-green-100 text-green-700'
                    : c.status === 'Blocked' ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-600';
                  return (
                    <tr key={c.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm font-medium text-gray-900 whitespace-nowrap">{c.cardHolderName || '—'}</td>
                      <td className="px-4 py-3 text-sm font-mono text-gray-600 whitespace-nowrap">••{c.cardNumber.slice(-4)}</td>
                      <td className="px-4 py-3">
                        <span className="text-xs font-semibold px-2 py-0.5 rounded-full"
                          style={{ background: `${CATEGORY_COLORS[c.cardType?.toUpperCase()] || '#94a3b8'}22`, color: CATEGORY_COLORS[c.cardType?.toUpperCase()] || '#475569' }}>
                          {c.cardType}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-700 whitespace-nowrap">{c.vendor}</td>
                      <td className="px-4 py-3">
                        <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${statusColor}`}>{c.status}</span>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-700 whitespace-nowrap">{inr(c.creditLimit)}</td>
                      <td className="px-4 py-3 text-sm text-gray-700 whitespace-nowrap">{inr(c.availableCredit)}</td>
                      <td className="px-4 py-3 text-sm text-gray-700 whitespace-nowrap">{inr(c.outstandingAmount)}</td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2 min-w-[120px]">
                          <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                            <div className={`h-full rounded-full ${u > 70 ? 'bg-red-500' : u > 40 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                              style={{ width: `${Math.min(u, 100)}%` }} />
                          </div>
                          <span className="text-xs font-semibold text-gray-700 w-9 text-right">{u}%</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600 whitespace-nowrap">{c.mobileNumber || '—'}</td>
                    </tr>
                  );
                })}
              {filteredCards.length === 0 && (
                <tr><td colSpan="10" className="px-6 py-10 text-center text-gray-500 text-sm">
                  {cardQuery ? 'No cards match your search' : 'No cards in portfolio'}
                </td></tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager
          page={allPageSafe}
          totalPages={allTotalPages}
          onChange={setAllPage}
          label={`Showing ${filteredCards.length === 0 ? 0 : (allPageSafe - 1) * ALL_PAGE_SIZE + 1}-${Math.min(allPageSafe * ALL_PAGE_SIZE, filteredCards.length)} of ${filteredCards.length}`}
        />
      </div>
    </div>
  );
};

export default CreditCardAnalytics;
