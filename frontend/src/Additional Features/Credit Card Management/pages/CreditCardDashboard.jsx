import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService, selectedCard } from '../services/creditCardsService';
import CreditCardWidget from '../components/CreditCardWidget';
import UtilizationBar from '../components/UtilizationBar';
import { AlertCircle, CreditCard, ArrowRight, CheckCircle, ChevronDown, Receipt, FileText, BarChart3, Search, X, SlidersHorizontal } from 'lucide-react';
import toast from 'react-hot-toast';

const CreditCardDashboard = () => {
  const [cards, setCards] = useState([]);
  const [selectedCardId, setSelectedCardId] = useState('');
  
  const [data, setData] = useState(null);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState('All');

  // Card search & filters (replaces the old selector dropdown)
  const [cardSearch, setCardSearch] = useState('');
  const [catFilter, setCatFilter] = useState('All');
  const [vendorFilter, setVendorFilter] = useState('All');
  const [statusFilter, setStatusFilter] = useState('All');

  const navigate = useNavigate();

  // Load all cards initially
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        setLoading(true);
        // Portfolio-wide listing keeps the selector in sync with Analytics (single source).
        // The whole book is searchable here; the search panel filters by parameters.
        const portfolio = await creditCardService.getAllCardsPortfolio();
        setCards(portfolio);

        if (portfolio && portfolio.length > 0) {
          // Restore the previously selected card if it still exists, else default to first.
          const saved = selectedCard.get();
          const initial = portfolio.some((c) => c.id === saved) ? saved : portfolio[0].id;
          setSelectedCardId(initial);
          selectedCard.set(initial);
        } else {
          setLoading(false); // No cards, stop loading
        }
      } catch (error) {
        toast.error('Unable to Load Cards');
        setLoading(false);
      }
    };
    fetchInitialData();
  }, []);

  // Reload specific card data when selection changes
  useEffect(() => {
    if (!selectedCardId) return;

    const loadCardData = async () => {
      try {
        setLoading(true);
        const cardData = await creditCardService.getDashboardData(selectedCardId);
        setData(cardData);
        
        if (cardData) {
          const txns = await creditCardService.getTransactions(null, selectedCardId);
          setRecentTransactions(txns.slice(0, 5));
        }
      } catch (error) {
        toast.error('Unable to Load Card Details');
      } finally {
        setLoading(false);
      }
    };
    loadCardData();
  }, [selectedCardId]);

  const isPaymentDueSoon = () => {
    if (!data) return false;
    const dueDate = new Date(data.nextDueDate);
    const today = new Date();
    const diffTime = dueDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays >= 0;
  };

  const showWarning = isPaymentDueSoon() && data.outstandingAmount > 0;



  const filteredTransactions = recentTransactions.filter(
    tx => filterType === 'All' ? true : tx.type === filterType
  );

  // Apply search + filters to the card portfolio
  const matchedCards = cards.filter((c) => {
    const q = cardSearch.trim().toLowerCase();
    const matchesSearch =
      !q ||
      (c.cardHolderName || '').toLowerCase().includes(q) ||
      (c.cardNumber || '').toLowerCase().includes(q) ||
      (c.cardNumber || '').slice(-4).includes(q) ||
      String(c.mobileNumber || '').includes(q) ||
      String(c.linkedAccountNumber || '').includes(q);
    const matchesCat = catFilter === 'All' || c.cardType === catFilter;
    const matchesVendor = vendorFilter === 'All' || c.vendor === vendorFilter;
    const matchesStatus = statusFilter === 'All' || c.status === statusFilter;
    return matchesSearch && matchesCat && matchesVendor && matchesStatus;
  });

  const hasActiveFilters =
    cardSearch || catFilter !== 'All' || vendorFilter !== 'All' || statusFilter !== 'All';

  const resetCardFilters = () => {
    setCardSearch('');
    setCatFilter('All');
    setVendorFilter('All');
    setStatusFilter('All');
  };

  const selectCard = (id) => {
    setSelectedCardId(id);
    selectedCard.set(id);
  };

  const statusPill = (status) => {
    const map = {
      Active: 'bg-green-100 text-green-700',
      Blocked: 'bg-red-100 text-red-700',
      Inactive: 'bg-gray-100 text-gray-600',
    };
    return map[status] || 'bg-gray-100 text-gray-600';
  };

  if (loading && cards.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Credit Card Administration</h1>
          <p className="text-gray-500">Search the card portfolio and review details, statements and transactions</p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={() => navigate('/credit-cards/analytics')}
            className="btn-outline flex items-center gap-2 shadow-sm"
          >
            <BarChart3 className="w-4 h-4" />
            Analytics
          </button>

          <button
            onClick={() => navigate('/credit-cards/apply')}
            className="btn-primary flex items-center gap-2 shadow-sm"
          >
            <CreditCard className="w-4 h-4" />
            Apply New Card
          </button>
        </div>
      </div>

      {/* Card search & filters */}
      {cards.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 space-y-4">
          <div className="flex flex-col lg:flex-row gap-3">
            {/* Search */}
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                value={cardSearch}
                onChange={(e) => setCardSearch(e.target.value)}
                placeholder="Search by holder name, card number, mobile or linked account…"
                className="w-full pl-10 pr-10 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
              {cardSearch && (
                <button
                  onClick={() => setCardSearch('')}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  <X className="w-4 h-4" />
                </button>
              )}
            </div>

            {/* Filters */}
            <div className="flex flex-wrap items-center gap-2">
              <SlidersHorizontal className="w-4 h-4 text-gray-400 hidden sm:block" />
              <select value={catFilter} onChange={(e) => setCatFilter(e.target.value)}
                className="py-2.5 px-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500">
                <option value="All">All Categories</option>
                <option value="Silver">Silver</option>
                <option value="Gold">Gold</option>
                <option value="Platinum">Platinum</option>
              </select>
              <select value={vendorFilter} onChange={(e) => setVendorFilter(e.target.value)}
                className="py-2.5 px-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500">
                <option value="All">All Vendors</option>
                <option value="VISA">VISA</option>
                <option value="MASTERCARD">Mastercard</option>
                <option value="RUPAY">RuPay</option>
              </select>
              <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}
                className="py-2.5 px-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500">
                <option value="All">All Status</option>
                <option value="Active">Active</option>
                <option value="Inactive">Inactive</option>
                <option value="Blocked">Blocked</option>
              </select>
              {hasActiveFilters && (
                <button onClick={resetCardFilters}
                  className="text-sm text-gray-500 hover:text-gray-700 underline px-1">
                  Clear
                </button>
              )}
            </div>
          </div>

          {/* Results */}
          <div className="text-xs text-gray-500">
            {matchedCards.length} of {cards.length} cards
          </div>
          <div className="max-h-72 overflow-y-auto divide-y divide-gray-100 border border-gray-100 rounded-lg">
            {matchedCards.length === 0 ? (
              <div className="p-6 text-center text-sm text-gray-500">
                No cards match your search.
              </div>
            ) : (
              matchedCards.map((c) => (
                <button
                  key={c.id}
                  onClick={() => selectCard(c.id)}
                  className={`w-full text-left px-4 py-3 flex items-center justify-between gap-3 transition-colors ${
                    c.id === selectedCardId ? 'bg-primary-50' : 'hover:bg-gray-50'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="w-9 h-9 rounded-lg bg-gray-100 flex items-center justify-center flex-shrink-0">
                      <CreditCard className="w-4 h-4 text-gray-500" />
                    </div>
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {c.cardHolderName || 'Unknown holder'}
                        <span className="text-gray-400 font-normal"> · ••{c.cardNumber.slice(-4)}</span>
                      </p>
                      <p className="text-xs text-gray-500 truncate">
                        {c.cardType} · {c.vendor} · ₹{Number(c.outstandingAmount).toLocaleString('en-IN')} outstanding
                      </p>
                    </div>
                  </div>
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full flex-shrink-0 ${statusPill(c.status)}`}>
                    {c.status}
                  </span>
                </button>
              ))
            )}
          </div>
        </div>
      )}

      {loading && cards.length > 0 && (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        </div>
      )}

      {!loading && showWarning && (
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded-md shadow-sm flex items-start">
          <AlertCircle className="w-5 h-5 text-yellow-500 mt-0.5 mr-3 flex-shrink-0" />
          <div>
            <h3 className="text-sm font-medium text-yellow-800">Payment Due Soon</h3>
            <p className="text-sm text-yellow-700 mt-1">
              Your minimum payment of ₹{data.minimumDue.toLocaleString('en-IN')} is due on {new Date(data.nextDueDate).toLocaleDateString()}. Please pay on time to avoid late fees.
            </p>
          </div>
        </div>
      )}

      {!loading && data?.outstandingAmount === 0 && (
        <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded-md shadow-sm flex items-start mb-4">
          <CheckCircle className="w-5 h-5 text-green-500 mt-0.5 mr-3 flex-shrink-0" />
          <div>
            <h3 className="text-sm font-medium text-green-800">Outstanding Bill: ₹0</h3>
            <p className="text-sm text-green-700 mt-1">
              You have no pending dues. Please add more money or spend more!
            </p>
          </div>
        </div>
      )}

      {!loading && data ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Card Visual & Util */}
          <div className="lg:col-span-1 space-y-6">
            <CreditCardWidget data={data} />
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <UtilizationBar available={data.availableCredit} limit={data.creditLimit} />
            </div>
          </div>

          {/* Details & Transactions */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Account Summary */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
                <h3 className="font-semibold text-gray-800">Account Summary</h3>
                <span className="text-xs font-medium bg-primary-100 text-primary-700 px-2.5 py-1 rounded-full">
                  {data.status}
                </span>
              </div>
              <div className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div>
                  <p className="text-sm text-gray-500">Outstanding Amount</p>
                  <p className="text-2xl font-bold text-gray-900">₹{data.outstandingAmount.toLocaleString('en-IN')}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Available Credit</p>
                  <p className="text-2xl font-bold text-primary-600">₹{data.availableCredit.toLocaleString('en-IN')}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Minimum Due</p>
                  <p className="text-lg font-semibold text-gray-800">₹{data.minimumDue.toLocaleString('en-IN')}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Next Due Date</p>
                  <p className="text-lg font-semibold text-gray-800">{new Date(data.nextDueDate).toLocaleDateString()}</p>
                </div>
              </div>
            </div>

            {/* Dashboard Recent Transactions with Dropdown */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
              <div className="px-6 py-4 border-b border-gray-100 bg-white flex justify-between items-center">
                <h3 className="font-semibold text-gray-800">Recent Activity</h3>
                <div className="relative">
                  <select 
                    value={filterType}
                    onChange={(e) => setFilterType(e.target.value)}
                    className="appearance-none bg-gray-50 border border-gray-200 text-gray-700 py-1.5 pl-3 pr-8 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-primary-500 focus:border-primary-500 cursor-pointer"
                  >
                    <option value="All">All Transactions</option>
                    <option value="Purchase">Purchases Only</option>
                    <option value="Payment">Payments Only</option>
                  </select>
                  <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-500">
                    <ChevronDown className="w-3.5 h-3.5" />
                  </div>
                </div>
              </div>
              
              <div className="divide-y divide-gray-100">
                {filteredTransactions.length === 0 ? (
                  <div className="p-6 text-center text-gray-500 text-sm">
                    No recent activity found for this filter.
                  </div>
                ) : (
                  filteredTransactions.map(tx => (
                    <div key={tx.id} className="p-4 px-6 flex justify-between items-center hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                          tx.type === 'Payment' ? 'bg-green-100 text-green-600' : 'bg-blue-100 text-blue-600'
                        }`}>
                          {tx.type === 'Payment' ? <Receipt className="w-5 h-5" /> : <FileText className="w-5 h-5" />}
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">{tx.merchant}</p>
                          <p className="text-xs text-gray-500">{new Date(tx.date).toLocaleDateString()}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className={`text-sm font-bold ${tx.type === 'Payment' ? 'text-green-600' : 'text-gray-900'}`}>
                          {tx.type === 'Payment' ? '+' : '-'} ₹{tx.amount.toLocaleString('en-IN')}
                        </p>
                        <p className="text-xs text-gray-500">{tx.status}</p>
                      </div>
                    </div>
                  ))
                )}
              </div>
              
              <div className="px-6 py-3 border-t border-gray-100 bg-gray-50 text-center">
                <button 
                  onClick={() => navigate('/credit-cards/transactions', { state: { cardId: selectedCardId } })}
                  className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center justify-center w-full gap-1"
                >
                  View All Transactions <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>

          </div>
        </div>
      ) : !loading && cards.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-gray-100">
          <CreditCard className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900">No active credit card</h3>
          <p className="text-gray-500 mt-2 mb-6">Apply for a new credit card to get started.</p>
          <button
            onClick={() => navigate('/credit-cards/apply')}
            className="btn-primary"
          >
            Apply Now
          </button>
        </div>
      ) : null}
    </div>
  );
};

export default CreditCardDashboard;
