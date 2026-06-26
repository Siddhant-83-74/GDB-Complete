import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/creditCardsService';
import { ArrowLeft, Shield, Clock, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

const CreditCardDetails = () => {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const cardData = await creditCardService.getDashboardData();
      
      // Validations
      if (cardData.availableCredit < 0 || cardData.outstandingAmount < 0 || cardData.creditLimit < 0) {
        throw new Error("Invalid Data: Values cannot be negative");
      }
      if (cardData.availableCredit > cardData.creditLimit) {
        throw new Error("Invalid Data: Available Credit cannot exceed Credit Limit");
      }
      if (cardData.outstandingAmount > cardData.creditLimit) {
        throw new Error("Invalid Data: Outstanding Amount cannot exceed Credit Limit");
      }

      setData(cardData);
    } catch (err) {
      setError(err.message);
      toast.error('Unable to Load Data');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-gray-100">
        <AlertCircle className="w-16 h-16 text-red-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900">Data Validation Error</h3>
        <p className="text-red-500 mt-2">{error}</p>
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button onClick={() => navigate('/credit-cards')} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <h1 className="text-2xl font-bold text-gray-900">Credit Card Details</h1>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="border-b border-gray-100 p-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-gray-50/30">
          <div className="flex items-center gap-4">
            <div className="w-16 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
              <Shield className="w-6 h-6 text-primary-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500 font-medium">Card Number</p>
              <p className="text-lg font-mono font-bold text-gray-900 tracking-wider">{data.cardNumber}</p>
            </div>
          </div>
          <div className="flex items-center gap-3 bg-white px-4 py-2 rounded-full border border-gray-200 shadow-sm">
            <span className={`w-2 h-2 rounded-full ${data.status === 'Active' ? 'bg-green-500' : 'bg-red-500'}`}></span>
            <span className="text-sm font-medium text-gray-700">{data.status}</span>
          </div>
        </div>

        <div className="p-6 sm:p-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-y-8 gap-x-12">
            
            <div>
              <p className="text-sm text-gray-500 mb-1">Card Type</p>
              <p className="text-base font-semibold text-gray-900">{data.cardType}</p>
            </div>

            <div>
              <p className="text-sm text-gray-500 mb-1">Total Credit Limit</p>
              <p className="text-base font-semibold text-gray-900">₹{data.creditLimit.toLocaleString('en-IN')}</p>
            </div>

            <div className="bg-green-50 p-4 rounded-lg border border-green-100">
              <p className="text-sm text-green-800 mb-1">Available Credit</p>
              <p className="text-xl font-bold text-green-700">₹{data.availableCredit.toLocaleString('en-IN')}</p>
            </div>

            <div className="bg-red-50 p-4 rounded-lg border border-red-100">
              <p className="text-sm text-red-800 mb-1">Outstanding Amount</p>
              <p className="text-xl font-bold text-red-700">₹{data.outstandingAmount.toLocaleString('en-IN')}</p>
            </div>

            <div className="col-span-1 md:col-span-2 border-t border-gray-100 pt-8">
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-6">Payment Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 bg-gray-50 p-6 rounded-xl border border-gray-200">
                <div className="flex items-start gap-4">
                  <div className="p-2 bg-white rounded-md shadow-sm">
                    <IndianRupee className="w-5 h-5 text-gray-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Minimum Amount Due</p>
                    <p className="text-lg font-bold text-gray-900">₹{data.minimumDue.toLocaleString('en-IN')}</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="p-2 bg-white rounded-md shadow-sm">
                    <Clock className="w-5 h-5 text-gray-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Payment Due Date</p>
                    <p className="text-lg font-bold text-gray-900">{new Date(data.nextDueDate).toLocaleDateString('en-IN', { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' })}</p>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
};

// Lucide icon helper
const IndianRupee = ({ className }) => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}>
    <path d="M6 3h12M6 8h12M6 13l7.5-7.5M6 13h3c3.31 0 6-2.69 6-6s-2.69-6-6-6M13 13l7.5 7.5"/>
  </svg>
);

export default CreditCardDetails;
