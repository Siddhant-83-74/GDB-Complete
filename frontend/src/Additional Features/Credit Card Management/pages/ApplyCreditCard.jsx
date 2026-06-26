import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/creditCardsService';
import { Briefcase, IndianRupee, CreditCard, CheckCircle, ArrowLeft, ShieldCheck, Star } from 'lucide-react';
import toast from 'react-hot-toast';

const ApplyCreditCard = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    employmentType: '',
    salary: '',
    cardType: ''
  });
  const [touched, setTouched] = useState({
    employmentType: false,
    salary: false,
    cardType: false
  });

  const validate = () => {
    const errors = {};
    if (!formData.employmentType) errors.employmentType = 'Employment Type is required';
    
    if (!formData.salary) {
      errors.salary = 'Salary is required';
    } else if (!/^\d+$/.test(formData.salary)) {
      errors.salary = 'Only numbers allowed';
    } else if (parseInt(formData.salary) <= 0) {
      errors.salary = 'Salary must be greater than zero';
    } else if (formData.salary.length > 8) {
      errors.salary = 'Salary maximum 8 digits allowed';
    }

    if (!formData.cardType) errors.cardType = 'Please select a card type';

    return errors;
  };

  const errors = validate();
  const isValid = Object.keys(errors).length === 0;

  const handleBlur = (field) => {
    setTouched({ ...touched, [field]: true });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isValid) {
      toast.error('Please fix the validation errors');
      setTouched({ employmentType: true, salary: true, cardType: true });
      return;
    }

    try {
      setLoading(true);
      const res = await creditCardService.applyForCard(formData);
      if (res.success) {
        toast.success('Application Submitted Successfully!');
        navigate('/credit-cards/application-success', { state: { applicationId: res.applicationId } });
      }
    } catch (error) {
      toast.error(error.message || 'Application Failed');
    } finally {
      setLoading(false);
    }
  };

  const cardOptions = [
    {
      type: 'Silver',
      bgClass: 'bg-gradient-to-br from-gray-100 to-gray-200',
      textClass: 'text-gray-800',
      iconClass: 'text-gray-500',
      borderClass: 'border-gray-300',
      activeRing: 'ring-gray-400',
      features: ['Zero joining fee', '1% Cashback']
    },
    {
      type: 'Gold',
      bgClass: 'bg-gradient-to-br from-yellow-50 to-amber-100',
      textClass: 'text-amber-900',
      iconClass: 'text-amber-500',
      borderClass: 'border-amber-300',
      activeRing: 'ring-amber-400',
      features: ['Lounge access', '2% Cashback']
    },
    {
      type: 'Platinum',
      bgClass: 'bg-gradient-to-br from-gray-800 to-gray-900',
      textClass: 'text-white',
      iconClass: 'text-gray-300',
      borderClass: 'border-gray-700',
      activeRing: 'ring-gray-900',
      features: ['Premium rewards', 'Travel insurance']
    }
  ];

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      {/* Header Section */}
      <div className="flex items-center gap-4 mb-2">
        <button 
          onClick={() => navigate('/credit-cards')} 
          className="p-2.5 bg-white shadow-sm hover:bg-gray-50 hover:shadow-md rounded-full transition-all border border-gray-100"
        >
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Apply for Credit Card</h1>
          <p className="text-gray-500 mt-1">Unlock premium benefits and unmatched rewards.</p>
        </div>
      </div>

      <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl border border-white/50 overflow-hidden relative">
        {/* Decorative Blob */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-primary-100 rounded-full mix-blend-multiply filter blur-3xl opacity-50 -translate-y-1/2 translate-x-1/2 pointer-events-none"></div>
        
        <div className="p-8 sm:p-10 relative z-10">
          <form onSubmit={handleSubmit} className="space-y-10">
            
            {/* Section 1: Professional Details */}
            <div className="space-y-6">
              <div className="flex items-center gap-2 border-b border-gray-100 pb-2">
                <Briefcase className="w-5 h-5 text-primary-600" />
                <h2 className="text-lg font-semibold text-gray-800">Professional Details</h2>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Employment Type */}
                <div className="group">
                  <label className="block text-sm font-semibold text-gray-700 mb-2 group-focus-within:text-primary-600 transition-colors">
                    Employment Type <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Briefcase className={`h-5 w-5 transition-colors ${touched.employmentType ? (errors.employmentType ? 'text-red-400' : 'text-green-500') : 'text-gray-400 group-focus-within:text-primary-500'}`} />
                    </div>
                    <select
                      className={`pl-12 block w-full rounded-xl shadow-sm sm:text-sm py-3 transition-all duration-200 ${
                        touched.employmentType && errors.employmentType
                          ? 'border-red-300 bg-red-50 focus:ring-red-500 focus:border-red-500'
                          : 'border-gray-200 bg-gray-50 focus:bg-white focus:ring-2 focus:ring-primary-500 focus:border-primary-500 hover:border-gray-300'
                      }`}
                      value={formData.employmentType}
                      onChange={(e) => setFormData({ ...formData, employmentType: e.target.value })}
                      onBlur={() => handleBlur('employmentType')}
                    >
                      <option value="">Select Employment Type</option>
                      <option value="Salaried">Salaried</option>
                      <option value="Self-Employed">Self-Employed</option>
                      <option value="Business Owner">Business Owner</option>
                    </select>
                  </div>
                  {touched.employmentType && errors.employmentType && (
                    <p className="mt-2 text-sm text-red-500 font-medium animate-pulse">{errors.employmentType}</p>
                  )}
                </div>

                {/* Monthly Salary */}
                <div className="group">
                  <label className="block text-sm font-semibold text-gray-700 mb-2 group-focus-within:text-primary-600 transition-colors">
                    Monthly Net Salary (₹) <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <IndianRupee className={`h-5 w-5 transition-colors ${touched.salary ? (errors.salary ? 'text-red-400' : 'text-green-500') : 'text-gray-400 group-focus-within:text-primary-500'}`} />
                    </div>
                    <input
                      type="text"
                      className={`pl-12 block w-full rounded-xl shadow-sm sm:text-sm py-3 transition-all duration-200 ${
                        touched.salary && errors.salary
                          ? 'border-red-300 bg-red-50 focus:ring-red-500 focus:border-red-500'
                          : 'border-gray-200 bg-gray-50 focus:bg-white focus:ring-2 focus:ring-primary-500 focus:border-primary-500 hover:border-gray-300'
                      }`}
                      placeholder="e.g. 50000"
                      value={formData.salary}
                      onChange={(e) => setFormData({ ...formData, salary: e.target.value })}
                      onBlur={() => handleBlur('salary')}
                    />
                  </div>
                  {touched.salary && errors.salary && (
                    <p className="mt-2 text-sm text-red-500 font-medium animate-pulse">{errors.salary}</p>
                  )}
                </div>
              </div>
            </div>

            {/* Section 2: Card Selection */}
            <div className="space-y-6">
              <div className="flex items-center gap-2 border-b border-gray-100 pb-2">
                <Star className="w-5 h-5 text-primary-600" />
                <h2 className="text-lg font-semibold text-gray-800">Select Card Type <span className="text-red-500">*</span></h2>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {cardOptions.map((card) => {
                  const isSelected = formData.cardType === card.type;
                  return (
                    <div
                      key={card.type}
                      onClick={() => setFormData({ ...formData, cardType: card.type })}
                      className={`cursor-pointer rounded-2xl p-6 transition-all duration-300 relative overflow-hidden group ${
                        isSelected 
                          ? `shadow-xl ring-2 ${card.activeRing} scale-[1.02] ${card.bgClass}` 
                          : `shadow-sm border ${card.borderClass} bg-white hover:shadow-md hover:-translate-y-1`
                      }`}
                    >
                      {/* Selection checkmark */}
                      {isSelected && (
                        <div className="absolute top-4 right-4">
                          <CheckCircle className={`w-6 h-6 ${card.type === 'Platinum' ? 'text-green-400' : 'text-green-500'}`} />
                        </div>
                      )}

                      <CreditCard className={`w-10 h-10 mb-4 transition-transform duration-300 group-hover:scale-110 ${isSelected ? (card.type === 'Platinum' ? 'text-gray-300' : card.iconClass) : 'text-gray-400'}`} />
                      
                      <h3 className={`text-xl font-bold mb-3 ${isSelected ? card.textClass : 'text-gray-900'}`}>
                        {card.type}
                      </h3>
                      
                      <ul className="space-y-2">
                        {card.features.map((feature, idx) => (
                          <li key={idx} className={`text-sm flex items-center gap-2 ${isSelected ? card.textClass : 'text-gray-600'}`}>
                            <ShieldCheck className={`w-4 h-4 ${isSelected ? 'opacity-80' : 'text-gray-400'}`} />
                            <span className={isSelected ? 'opacity-90' : ''}>{feature}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )
                })}
              </div>
              {touched.cardType && errors.cardType && (
                <p className="mt-2 text-sm text-red-500 font-medium text-center animate-pulse">{errors.cardType}</p>
              )}
            </div>

            {/* Submit Button */}
            <div className="pt-8 mt-8 border-t border-gray-100 flex justify-center">
              <button
                type="submit"
                disabled={!isValid || loading}
                className="w-full sm:w-auto min-w-[240px] flex justify-center items-center py-4 px-8 rounded-xl shadow-lg text-base font-bold text-white bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all hover:shadow-primary-500/25 duration-300 transform active:scale-95"
              >
                {loading ? (
                  <div className="flex items-center gap-3">
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Processing Application...</span>
                  </div>
                ) : (
                  <span>Submit Application</span>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ApplyCreditCard;
