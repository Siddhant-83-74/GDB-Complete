import React from 'react';
import { Wifi, Globe } from 'lucide-react';

const CreditCardWidget = ({ data }) => {
  if (!data) return null;

  // Determine card style based on type
  const getCardStyle = (type) => {
    switch (type) {
      case 'Platinum':
        return 'from-gray-900 to-gray-700 text-white';
      case 'Gold':
        return 'from-yellow-500 to-yellow-300 text-yellow-900';
      case 'Silver':
      default:
        return 'from-gray-300 to-gray-100 text-gray-800';
    }
  };

  const cardStyle = getCardStyle(data.cardType);

  // Format expiry (YYYY-MM-DD) as MM/YY
  const validThru = (() => {
    if (!data.expiryDate) return '--/--';
    const d = new Date(data.expiryDate);
    if (isNaN(d.getTime())) return '--/--';
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yy = String(d.getFullYear()).slice(-2);
    return `${mm}/${yy}`;
  })();

  const inr = (n) => `₹${Number(n || 0).toLocaleString('en-IN')}`;
  const holder = (data.cardHolderName || 'Card Holder').toUpperCase();
  const brand = (data.vendor || '').toUpperCase();

  return (
    <div className={`relative w-full max-w-sm rounded-2xl p-6 shadow-xl bg-gradient-to-br ${cardStyle} overflow-hidden`}>
      {/* Decorative elements */}
      <div className="absolute top-0 right-0 -mr-8 -mt-8 w-32 h-32 rounded-full bg-white opacity-10"></div>
      <div className="absolute bottom-0 left-0 -ml-8 -mb-8 w-24 h-24 rounded-full bg-white opacity-10"></div>

      <div className="relative z-10 flex flex-col h-full justify-between gap-5">
        {/* Top: brand tier + bank */}
        <div className="flex justify-between items-start">
          <div className="flex flex-col">
            <span className="text-sm font-semibold opacity-90 uppercase tracking-wider">{data.cardType}</span>
            <span className="text-xs opacity-75 mt-1">Global Digital Bank</span>
          </div>
          <div className="flex items-center gap-2">
            {data.internationalEnabled && <Globe className="w-4 h-4 opacity-80" title="International enabled" />}
            <Wifi className="w-6 h-6 transform rotate-90 opacity-80" />
          </div>
        </div>

        {/* Chip + number */}
        <div className="flex items-center gap-4">
          <div className="w-12 h-8 bg-yellow-200 rounded-md opacity-80"></div>
          <div className="tracking-[0.2em] font-mono text-lg lg:text-xl font-medium">
            {data.cardNumber}
          </div>
        </div>

        {/* Valid thru + mobile */}
        <div className="flex justify-between items-end -mt-1">
          <div className="flex flex-col">
            <span className="text-[10px] uppercase opacity-75 tracking-wider">Valid Thru</span>
            <span className="font-medium tracking-wide text-sm">{validThru}</span>
          </div>
          {data.mobileNumber && (
            <div className="flex flex-col items-end">
              <span className="text-[10px] uppercase opacity-75 tracking-wider">Mobile</span>
              <span className="font-medium tracking-wide text-sm">{data.mobileNumber}</span>
            </div>
          )}
        </div>

        {/* Holder + brand/status */}
        <div className="flex justify-between items-end">
          <div className="flex flex-col min-w-0 pr-2">
            <span className="text-[10px] uppercase opacity-75 tracking-wider">Card Holder</span>
            <span className="font-semibold tracking-wide truncate">{holder}</span>
          </div>
          <div className="flex flex-col items-end">
            <span className="text-[10px] uppercase opacity-75 tracking-wider">{data.status}</span>
            <span className="font-bold tracking-wider italic text-base">{brand}</span>
          </div>
        </div>

        {/* Limit / available footer */}
        <div className="flex justify-between items-center pt-3 mt-1 border-t border-current/20 text-xs">
          <div className="flex flex-col">
            <span className="opacity-70">Credit Limit</span>
            <span className="font-semibold">{inr(data.creditLimit)}</span>
          </div>
          <div className="flex flex-col items-end">
            <span className="opacity-70">Available</span>
            <span className="font-semibold">{inr(data.availableCredit)}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreditCardWidget;
