import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/creditCardsService';
import { ArrowLeft, IndianRupee, Building2, AlertCircle, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';

const PayCreditCardBill = () => {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  
  const [formData, setFormData] = useState({
    amount: '',
    debitAccount: ''
  });
  const [touched, setTouched] = useState({
    amount: false,
    debitAccount: false
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const cardData = await creditCardService.getDashboardData();
      setData(cardData);
      // Pre-fill minimum due as default amount
      if (cardData.minimumDue > 0) {
        setFormData(prev => ({ ...prev, amount: cardData.minimumDue.toString() }));
      }
    } catch (error) {
      toast.error('Unable to Load Data');
    } finally {
      setLoading(false);
    }
  };

  const validate = () => {
    const errors = {};
    if (!formData.debitAccount) errors.debitAccount = 'Debit Account is required';
    
    if (!formData.amount) {
      errors.amount = 'Amount is required';
    } else if (!/^\d+(\.\d{1,2})?$/.test(formData.amount)) {
      errors.amount = 'Valid numeric amount required (max 2 decimal places)';
    } else if (parseFloat(formData.amount) <= 0) {
      errors.amount = 'Amount must be greater than zero';
    } else if (data && parseFloat(formData.amount) > data.outstandingAmount) {
      errors.amount = 'Amount cannot exceed Outstanding Amount';
    }

    return errors;
  };

  const errors = validate();
  const isValid = Object.keys(errors).length === 0;

  const handleBlur = (field) => {
    setTouched({ ...touched, [field]: true });
  };

  const handlePreviewSubmit = (e) => {
    e.preventDefault();
    if (!isValid) {
      toast.error('Validation Failed');
      setTouched({ amount: true, debitAccount: true });
      return;
    }
    setShowConfirm(true);
  };

  const handleProcessPayment = async () => {
    try {
      setProcessing(true);
      const res = await creditCardService.payBill({
        amount: parseFloat(formData.amount),
        debitAccount: formData.debitAccount
      });
      
      if (res.success) {
        toast.success('Payment Successful');
        setShowConfirm(false);
        navigate('/credit-cards/payment-success', { 
          state: { 
            transactionId: res.transactionId,
            amount: formData.amount,
            account: formData.debitAccount
          } 
        });
      }
    } catch (error) {
      toast.error(error.message || 'Payment Failed');
      setShowConfirm(false);
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <button onClick={() => navigate('/credit-cards')} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <h1 className="text-2xl font-bold text-gray-900">Pay Credit Card Bill</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Outstanding 0 Pill */}
        {data?.outstandingAmount === 0 && (
          <div className="md:col-span-3 bg-green-50 border border-green-200 rounded-xl p-4 flex items-center gap-3">
            <CheckCircle className="w-6 h-6 text-green-500 flex-shrink-0" />
            <div>
              <p className="font-semibold text-green-800">Outstanding Bill: ₹0</p>
              <p className="text-sm text-green-700">You have no pending dues. Please spend more money!</p>
            </div>
          </div>
        )}

        {/* Payment Summary Panel */}
        <div className="md:col-span-1 space-y-6">
          <div className="bg-gradient-to-br from-gray-900 to-gray-800 rounded-xl p-6 text-white shadow-md">
            <p className="text-sm text-gray-300 mb-1">Outstanding Balance</p>
            <p className="text-3xl font-bold mb-6">₹{data?.outstandingAmount.toLocaleString('en-IN')}</p>
            
            <div className="space-y-3">
              <div className="flex justify-between items-center text-sm border-b border-gray-700 pb-2">
                <span className="text-gray-400">Minimum Due</span>
                <span className="font-semibold">₹{data?.minimumDue.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between items-center text-sm border-b border-gray-700 pb-2">
                <span className="text-gray-400">Due Date</span>
                <span className="font-semibold">{new Date(data?.nextDueDate).toLocaleDateString()}</span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="text-gray-400">Card Number</span>
                <span className="font-semibold">{data?.cardNumber}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Payment Form Panel */}
        <div className="md:col-span-2">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6">
              <form onSubmit={handlePreviewSubmit} className="space-y-6">
                
                {/* Debit Account */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Pay From Account *</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Building2 className="h-5 w-5 text-gray-400" />
                    </div>
                    <select
                      className={`pl-10 block w-full rounded-md shadow-sm sm:text-sm ${
                        touched.debitAccount && errors.debitAccount
                          ? 'border-red-300 focus:ring-red-500 focus:border-red-500'
                          : 'border-gray-300 focus:ring-primary-500 focus:border-primary-500'
                      }`}
                      value={formData.debitAccount}
                      onChange={(e) => setFormData({ ...formData, debitAccount: e.target.value })}
                      onBlur={() => handleBlur('debitAccount')}
                    >
                      <option value="">Select Account</option>
                      <option value="1001">Savings Account - 1001 (₹500,000)</option>
                      <option value="1002">Current Account - 1002 (₹1,000,000)</option>
                    </select>
                  </div>
                  {touched.debitAccount && errors.debitAccount && (
                    <p className="mt-1 text-sm text-red-600">{errors.debitAccount}</p>
                  )}
                </div>

                {/* Amount Options */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">Select Payment Amount</label>
                  <div className="grid grid-cols-2 gap-4 mb-4">
                    <div 
                      onClick={() => { setFormData({ ...formData, amount: data.outstandingAmount.toString() }); setTouched({ ...touched, amount: true }); }}
                      className={`cursor-pointer rounded-lg border p-3 text-center transition-all ${
                        formData.amount === data.outstandingAmount.toString() ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500' : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <p className="text-xs text-gray-500 mb-1">Total Outstanding</p>
                      <p className="font-semibold text-gray-900">₹{data.outstandingAmount.toLocaleString('en-IN')}</p>
                    </div>
                    <div 
                      onClick={() => { setFormData({ ...formData, amount: data.minimumDue.toString() }); setTouched({ ...touched, amount: true }); }}
                      className={`cursor-pointer rounded-lg border p-3 text-center transition-all ${
                        formData.amount === data.minimumDue.toString() ? 'border-primary-500 bg-primary-50 ring-1 ring-primary-500' : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <p className="text-xs text-gray-500 mb-1">Minimum Due</p>
                      <p className="font-semibold text-gray-900">₹{data.minimumDue.toLocaleString('en-IN')}</p>
                    </div>
                  </div>

                  <label className="block text-sm font-medium text-gray-700 mb-2">Or enter custom amount (₹) *</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <IndianRupee className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      type="text"
                      className={`pl-10 block w-full rounded-md shadow-sm sm:text-sm ${
                        touched.amount && errors.amount
                          ? 'border-red-300 focus:ring-red-500 focus:border-red-500'
                          : 'border-gray-300 focus:ring-primary-500 focus:border-primary-500'
                      }`}
                      placeholder="e.g. 5000"
                      value={formData.amount}
                      onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                      onBlur={() => handleBlur('amount')}
                    />
                  </div>
                  {touched.amount && errors.amount && (
                    <p className="mt-1 text-sm text-red-600">{errors.amount}</p>
                  )}
                </div>

                <div className="pt-4">
                  <button
                    type="submit"
                    disabled={!isValid || data?.outstandingAmount <= 0}
                    className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                  >
                    Proceed to Pay
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {showConfirm && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div className="fixed inset-0 transition-opacity" aria-hidden="true">
              <div className="absolute inset-0 bg-gray-500 opacity-75" onClick={() => !processing && setShowConfirm(false)}></div>
            </div>
            <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                <div className="sm:flex sm:items-start">
                  <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 sm:mx-0 sm:h-10 sm:w-10">
                    <AlertCircle className="h-6 w-6 text-blue-600" aria-hidden="true" />
                  </div>
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">Confirm Payment</h3>
                    <div className="mt-4 space-y-4">
                      <div className="bg-gray-50 p-4 rounded-md">
                        <div className="flex justify-between text-sm mb-2">
                          <span className="text-gray-500">From Account:</span>
                          <span className="font-medium text-gray-900">{formData.debitAccount}</span>
                        </div>
                        <div className="flex justify-between text-sm mb-2">
                          <span className="text-gray-500">To Card:</span>
                          <span className="font-medium text-gray-900">{data?.cardNumber}</span>
                        </div>
                        <div className="flex justify-between text-base border-t border-gray-200 pt-2 mt-2">
                          <span className="font-semibold text-gray-900">Payment Amount:</span>
                          <span className="font-bold text-primary-600">₹{parseFloat(formData.amount).toLocaleString('en-IN', {minimumFractionDigits: 2})}</span>
                        </div>
                      </div>
                      <p className="text-sm text-gray-500">
                        Please confirm the details above. This action cannot be undone.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  disabled={processing}
                  className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-70 disabled:cursor-not-allowed"
                  onClick={handleProcessPayment}
                >
                  {processing ? 'Processing...' : 'Confirm Payment'}
                </button>
                <button
                  type="button"
                  disabled={processing}
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={() => setShowConfirm(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PayCreditCardBill;
