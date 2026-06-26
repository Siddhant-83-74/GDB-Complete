/**
 * Real Credit Card service.
 *
 * Talks to credit-cards-service through the gateway and adapts the backend
 * payloads to the exact shape the existing pages expect (the same contract the
 * old mockCreditCardService exposed), so the page components need no changes
 * beyond their import path.
 */

import { creditCardsApi } from '../../../services/apiConfig';

const BASE = '/api/v1/credit-cards';

// --- helpers -------------------------------------------------------------

const titleCase = (s) =>
  typeof s === 'string' && s.length ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : s;

const getCurrentUserId = () => {
  // The auth store is persisted by zustand under 'gdb-auth-storage'.
  try {
    const raw = localStorage.getItem('gdb-auth-storage');
    if (raw) {
      const parsed = JSON.parse(raw);
      const user = parsed?.state?.user;
      const id = user?.user_id ?? user?.id;
      if (id != null) return id;
    }
  } catch (e) {
    console.error('Could not read user id from auth storage:', e);
  }
  // Fallback: decode the JWT.
  try {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.user_id ?? payload.id ?? payload.sub;
    }
  } catch (e) {
    console.error('Could not decode user id from token:', e);
  }
  return null;
};

// APPROVED -> Completed, ON_HOLD -> Pending, else title-cased
const mapTxnStatus = (status) => {
  switch (status) {
    case 'APPROVED':
      return 'Completed';
    case 'ON_HOLD':
      return 'Pending';
    case 'DECLINED':
      return 'Declined';
    case 'REVERSED':
      return 'Reversed';
    default:
      return titleCase(status);
  }
};

const adaptCard = (c) => {
  const outstanding = Number(c.outstanding_amount ?? 0);
  return {
    id: c.id,
    cardNumber: c.card_number, // already masked by the backend
    cardType: titleCase(c.category), // PLATINUM -> Platinum
    creditLimit: Number(c.credit_limit ?? 0),
    availableCredit: Number(c.available_credit ?? 0),
    outstandingAmount: outstanding,
    // The backend doesn't model a billing cycle yet; derive sensible display values.
    minimumDue: Math.round(outstanding * 0.05),
    nextDueDate: new Date(Date.now() + 20 * 24 * 60 * 60 * 1000).toISOString(),
    status: titleCase(c.status), // ACTIVE -> Active
    vendor: c.vendor,
    mobileNumber: c.mobile_number,
    internationalEnabled: c.international_enabled,
    linkedAccountNumber: c.linked_account_number,
  };
};

const adaptTxn = (t) => ({
  id: t.id,
  cardId: t.card_id,
  date: t.created_at,
  merchant: t.merchant,
  amount: Number(t.amount ?? 0),
  type: titleCase(t.type), // PURCHASE -> Purchase
  status: mapTxnStatus(t.status),
});

const errMsg = (error, fallback) =>
  error.response?.data?.message || error.response?.data?.detail || error.message || fallback;

// --- service -------------------------------------------------------------

export const creditCardService = {
  getAllCards: async () => {
    const userId = getCurrentUserId();
    if (userId == null) return [];
    try {
      const { data } = await creditCardsApi.get(`${BASE}/user/${userId}`);
      return (data || []).map(adaptCard);
    } catch (error) {
      throw new Error(errMsg(error, 'Failed to fetch cards'));
    }
  },

  getDashboardData: async (cardId = null) => {
    try {
      if (cardId) {
        const { data } = await creditCardsApi.get(`${BASE}/${cardId}`);
        return adaptCard(data);
      }
      const cards = await creditCardService.getAllCards();
      return cards.length ? cards[0] : null;
    } catch (error) {
      throw new Error(errMsg(error, 'Failed to fetch card details'));
    }
  },

  getTransactions: async (filters, cardId = null) => {
    try {
      const targetCardId = cardId || (await creditCardService.getAllCards())[0]?.id;
      if (!targetCardId) return [];
      const { data } = await creditCardsApi.get(`${BASE}/${targetCardId}/transactions`);
      let txns = (data || []).map(adaptTxn);

      if (filters) {
        if (filters.type && filters.type !== 'All') {
          txns = txns.filter((t) => t.type === filters.type);
        }
        if (filters.fromDate) {
          txns = txns.filter((t) => new Date(t.date) >= new Date(filters.fromDate));
        }
        if (filters.toDate) {
          const toEnd = new Date(filters.toDate);
          toEnd.setDate(toEnd.getDate() + 1);
          txns = txns.filter((t) => new Date(t.date) < toEnd);
        }
      }
      return txns;
    } catch (error) {
      throw new Error(errMsg(error, 'Failed to fetch transactions'));
    }
  },

  applyForCard: async (applicationData) => {
    if (!applicationData.employmentType || !applicationData.salary || !applicationData.cardType) {
      throw new Error('Missing required fields');
    }
    // Map the lightweight UI form onto the backend application contract.
    // Fields the form doesn't collect are defaulted sensibly.
    const userId = getCurrentUserId();
    let holderName = 'Card Holder';
    let mobile = '9999999999';
    try {
      const parsed = JSON.parse(localStorage.getItem('gdb-auth-storage'));
      holderName = parsed?.state?.user?.full_name || parsed?.state?.user?.username || holderName;
    } catch (e) {
      /* use default */
    }

    const expiry = new Date();
    expiry.setFullYear(expiry.getFullYear() + 4);

    const payload = {
      card_holder_name: holderName,
      user_id: userId,
      mobile_number: mobile,
      vendor: 'VISA',
      category: applicationData.cardType.toUpperCase(), // Silver -> SILVER
      expiry_date: expiry.toISOString().slice(0, 10),
      cvv: String(Math.floor(100 + Math.random() * 900)),
    };

    try {
      const { data } = await creditCardsApi.post(`${BASE}/apply`, payload);
      return {
        success: true,
        message: 'Application submitted successfully',
        applicationId: data.id,
      };
    } catch (error) {
      throw new Error(errMsg(error, 'Application failed'));
    }
  },

  payBill: async (paymentData, cardId = null) => {
    if (!paymentData.amount || paymentData.amount <= 0) {
      throw new Error('Invalid payment amount');
    }
    if (!paymentData.debitAccount) {
      throw new Error('Debit account is required');
    }
    try {
      const targetCardId = cardId || (await creditCardService.getAllCards())[0]?.id;
      if (!targetCardId) throw new Error('Card not found');
      const { data } = await creditCardsApi.post(`${BASE}/${targetCardId}/pay`, {
        amount: paymentData.amount,
        source_reference: String(paymentData.debitAccount),
      });
      return { success: true, transactionId: data.id };
    } catch (error) {
      throw new Error(errMsg(error, 'Payment failed'));
    }
  },
};
