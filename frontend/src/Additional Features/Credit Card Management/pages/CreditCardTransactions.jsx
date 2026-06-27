import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/creditCardsService';
import { ArrowLeft, Search, Filter, ArrowUpDown, ArrowUp, ArrowDown, Download, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

const CreditCardTransactions = () => {
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Filters
  const [filters, setFilters] = useState({
    fromDate: '',
    toDate: '',
    type: 'All'
  });
  const [activeFilters, setActiveFilters] = useState(null);
  
  // Search & Sort
  const [searchTerm, setSearchTerm] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: 'date', direction: 'desc' });
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    loadTransactions();
  }, [activeFilters]);

  const loadTransactions = async () => {
    try {
      setLoading(true);
      const data = await creditCardService.getTransactions(activeFilters);
      setTransactions(data);
      setCurrentPage(1); // Reset to first page on new data
    } catch (error) {
      toast.error('Unable to Load Transactions');
    } finally {
      setLoading(false);
    }
  };

  const validateFilters = () => {
    if (!filters.fromDate || !filters.toDate) {
      toast.error('Both From and To dates are required for filtering');
      return false;
    }
    
    const from = new Date(filters.fromDate);
    const to = new Date(filters.toDate);
    const today = new Date();
    
    if (from > to) {
      toast.error('From Date cannot exceed To Date');
      return false;
    }
    
    if (from > today || to > today) {
      toast.error('Future dates are not allowed');
      return false;
    }
    
    const diffTime = Math.abs(to - from);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays > 365) {
      toast.error('Maximum date range is 365 days');
      return false;
    }
    
    return true;
  };

  const handleApplyFilters = () => {
    if (filters.fromDate || filters.toDate) {
      if (!validateFilters()) return;
    }
    setActiveFilters({ ...filters });
  };

  const handleClearFilters = () => {
    setFilters({ fromDate: '', toDate: '', type: 'All' });
    setActiveFilters(null);
  };

  const handleSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  // Processed Data (Search, Sort)
  const processedTransactions = useMemo(() => {
    let processed = [...transactions];

    // Search
    if (searchTerm) {
      processed = processed.filter(t => 
        t.merchant.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.id.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Sort
    processed.sort((a, b) => {
      if (a[sortConfig.key] < b[sortConfig.key]) {
        return sortConfig.direction === 'asc' ? -1 : 1;
      }
      if (a[sortConfig.key] > b[sortConfig.key]) {
        return sortConfig.direction === 'asc' ? 1 : -1;
      }
      return 0;
    });

    return processed;
  }, [transactions, searchTerm, sortConfig]);

  // Pagination logic
  const totalPages = Math.ceil(processedTransactions.length / itemsPerPage);
  const currentTransactions = processedTransactions.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/credit-cards')} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Credit Card Transactions</h1>
            <p className="text-sm text-gray-500">Showing activity for the card selected on the dashboard</p>
          </div>
        </div>
        <button className="btn-outline flex items-center gap-2">
          <Download className="w-4 h-4" />
          Export Statement
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 space-y-4">
        <div className="flex flex-col lg:flex-row gap-4">
          {/* Search */}
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search by merchant or Ref ID..."
              value={searchTerm}
              onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(1); }}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          {/* Filters */}
          <div className="flex flex-wrap items-end gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">From Date</label>
              <input 
                type="date" 
                value={filters.fromDate}
                max={new Date().toISOString().split('T')[0]}
                onChange={(e) => setFilters({...filters, fromDate: e.target.value})}
                className="w-full py-2 px-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">To Date</label>
              <input 
                type="date" 
                value={filters.toDate}
                max={new Date().toISOString().split('T')[0]}
                onChange={(e) => setFilters({...filters, toDate: e.target.value})}
                className="w-full py-2 px-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Type</label>
              <select 
                value={filters.type}
                onChange={(e) => setFilters({...filters, type: e.target.value})}
                className="w-full py-2 px-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500"
              >
                <option value="All">All Types</option>
                <option value="Purchase">Purchase</option>
                <option value="Payment">Payment</option>
                <option value="Refund">Refund</option>
              </select>
            </div>
            <button onClick={handleApplyFilters} className="btn-primary py-2 px-4 h-[38px] flex items-center gap-2 text-sm">
              <Filter className="w-4 h-4" /> Apply
            </button>
            {(activeFilters?.fromDate || activeFilters?.type !== 'All') && (
              <button onClick={handleClearFilters} className="text-sm text-gray-500 hover:text-gray-700 underline px-2">
                Clear
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th onClick={() => handleSort('date')} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100">
                  <div className="flex items-center gap-1">Date {sortConfig.key === 'date' ? (sortConfig.direction === 'asc' ? <ArrowUp className="w-3 h-3"/> : <ArrowDown className="w-3 h-3"/>) : <ArrowUpDown className="w-3 h-3"/>}</div>
                </th>
                <th onClick={() => handleSort('merchant')} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100">
                  <div className="flex items-center gap-1">Merchant {sortConfig.key === 'merchant' ? (sortConfig.direction === 'asc' ? <ArrowUp className="w-3 h-3"/> : <ArrowDown className="w-3 h-3"/>) : <ArrowUpDown className="w-3 h-3"/>}</div>
                </th>
                <th onClick={() => handleSort('amount')} className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100">
                  <div className="flex items-center justify-end gap-1">Amount {sortConfig.key === 'amount' ? (sortConfig.direction === 'asc' ? <ArrowUp className="w-3 h-3"/> : <ArrowDown className="w-3 h-3"/>) : <ArrowUpDown className="w-3 h-3"/>}</div>
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {loading ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center">
                    <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                    <p className="mt-2 text-sm text-gray-500">Loading transactions...</p>
                  </td>
                </tr>
              ) : currentTransactions.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                    <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                    <p className="text-base font-medium text-gray-900">No transactions found</p>
                    <p className="text-sm">Try adjusting your filters or search term</p>
                  </td>
                </tr>
              ) : (
                currentTransactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{new Date(tx.date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</div>
                      <div className="text-xs text-gray-500">{tx.id}</div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">{tx.merchant}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <div className={`text-sm font-bold ${tx.type === 'Payment' || tx.type === 'Refund' ? 'text-green-600' : 'text-gray-900'}`}>
                        {tx.type === 'Payment' || tx.type === 'Refund' ? '+' : '-'} ₹{tx.amount.toLocaleString('en-IN')}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${tx.type === 'Purchase' ? 'bg-blue-100 text-blue-800' : ''}
                        ${tx.type === 'Payment' ? 'bg-green-100 text-green-800' : ''}
                        ${tx.type === 'Refund' ? 'bg-purple-100 text-purple-800' : ''}
                      `}>
                        {tx.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-1.5">
                        <span className={`w-1.5 h-1.5 rounded-full ${tx.status === 'Completed' ? 'bg-green-500' : 'bg-yellow-500'}`}></span>
                        <span className="text-sm text-gray-700">{tx.status}</span>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between bg-gray-50">
            <span className="text-sm text-gray-700">
              Showing <span className="font-medium">{(currentPage - 1) * itemsPerPage + 1}</span> to <span className="font-medium">{Math.min(currentPage * itemsPerPage, processedTransactions.length)}</span> of <span className="font-medium">{processedTransactions.length}</span> results
            </span>
            <div className="flex gap-2 items-center">
              <button
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                let pageNum;
                if (totalPages <= 5) {
                  pageNum = i + 1;
                } else if (currentPage <= 3) {
                  pageNum = i + 1;
                } else if (currentPage >= totalPages - 2) {
                  pageNum = totalPages - 4 + i;
                } else {
                  pageNum = currentPage - 2 + i;
                }
                return (
                  <button
                    key={pageNum}
                    onClick={() => setCurrentPage(pageNum)}
                    className={`px-3 py-1 rounded-md text-sm font-medium border ${
                      currentPage === pageNum
                        ? 'bg-primary-600 text-white border-primary-600'
                        : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                    }`}
                  >
                    {pageNum}
                  </button>
                );
              })}
              <button
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CreditCardTransactions;
