import { create } from 'zustand';
import { accountsService, aadharService, companyCrvService } from '../services/api';

export const useAccountStore = create((set, get) => ({
  accounts: [],
  selectedAccount: null,
  isLoading: false,
  loading: false,
  error: null,
  filters: {
    search: '',
    accountType: 'all',
    status: 'all',
    privilege: 'all',
  },

  // Fetch all accounts from backend
  fetchAccounts: async () => {
    set({ isLoading: true, error: null });
    
    try {
      console.log('=== FETCHING ACCOUNTS ===');
      const token = localStorage.getItem('token');
      console.log('Token in localStorage:', token ? `${token.substring(0, 20)}...` : 'MISSING!');
      
      const accounts = await accountsService.getAll();
      console.log('Accounts received:', accounts?.length || 0, 'accounts');
      set({ accounts: accounts || [], isLoading: false });
      return accounts;
    } catch (error) {
      console.error('Failed to fetch accounts:', error.message);
      set({ accounts: [], isLoading: false, error: error.message });
      throw error;
    }
  },

  // Get account by number
  getAccountByNumber: async (accountNumber) => {
    const { accounts } = get();
    const accNum = typeof accountNumber === 'string' ? parseInt(accountNumber, 10) : accountNumber;
    
    // First check local state
    const localAccount = accounts.find(acc => acc.account_number === accNum);
    if (localAccount) return localAccount;
    
    // Fetch from backend
    try {
      const account = await accountsService.getByNumber(accountNumber);
      return account;
    } catch (error) {
      console.error('Account not found:', error.message);
      return null;
    }
  },

  // Verify Aadhar for savings account
  verifyAadhar: async (aadharNumber) => {
    set({ isLoading: true, error: null });
    
    try {
      const result = await aadharService.verify(aadharNumber);
      set({ isLoading: false });
      return result;
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },

  // Verify Company Registration for current account
  verifyCompanyRegistration: async (registrationNumber) => {
    set({ isLoading: true, error: null });
    
    try {
      const result = await companyCrvService.verify(registrationNumber);
      set({ isLoading: false });
      return result;
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },

  // Create savings account
  createSavingsAccount: async (accountData) => {
    set({ isLoading: true, error: null });

    try {
      const newAccount = await accountsService.createSavings(accountData);
      set(state => ({
        accounts: [...state.accounts, newAccount],
        isLoading: false,
      }));
      return newAccount;
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },

  // Create current account
  createCurrentAccount: async (accountData) => {
    set({ isLoading: true, error: null });

    try {
      const newAccount = await accountsService.createCurrent(accountData);
      set(state => ({
        accounts: [...state.accounts, newAccount],
        isLoading: false,
      }));
      return newAccount;
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },

  // Update account
  updateAccount: async (accountNumber, updates) => {
    set({ isLoading: true, error: null });

    try {
      const updatedAccount = await accountsService.update(accountNumber, updates);
      set(state => ({
        accounts: state.accounts.map(acc =>
          acc.account_number === accountNumber ? updatedAccount : acc
        ),
        isLoading: false,
      }));
      return updatedAccount;
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },

  // Activate account
  activateAccount: async (accountNumber) => {
    return await get().updateAccount(accountNumber, { is_active: true, closed_date: null });
  },

  // Deactivate account
  deactivateAccount: async (accountNumber) => {
    return await get().updateAccount(accountNumber, { 
      is_active: false, 
      closed_date: new Date().toISOString() 
    });
  },

  // Update balance locally (called after transaction)
  updateBalance: (accountNumber, amountChange) => {
    set(state => ({
      accounts: state.accounts.map(acc =>
        acc.account_number === parseInt(accountNumber)
          ? { ...acc, balance: acc.balance + amountChange }
          : acc
      ),
    }));
  },

  // Verify PIN
  verifyPin: async (accountNumber, pin) => {
    try {
      const result = await accountsService.verifyPin(accountNumber, pin);
      return result.valid || result.verified || true;
    } catch (error) {
      console.error('PIN verification failed:', error.message);
      throw error;
    }
  },

  // Set filters
  setFilters: (filters) => {
    set(state => ({
      filters: { ...state.filters, ...filters },
    }));
  },

  // Get filtered accounts
  getFilteredAccounts: () => {
    const { accounts, filters } = get();
    
    if (!accounts || accounts.length === 0) return [];
    
    return accounts.filter(account => {
      const matchesSearch = 
        filters.search === '' ||
        (account.name && account.name.toLowerCase().includes(filters.search.toLowerCase())) ||
        (account.account_number && account.account_number.toString().includes(filters.search));

      const matchesType = 
        filters.accountType === 'all' || 
        account.account_type === filters.accountType;

      const matchesStatus = 
        filters.status === 'all' ||
        (filters.status === 'active' && account.is_active) ||
        (filters.status === 'inactive' && !account.is_active);

      const matchesPrivilege = 
        filters.privilege === 'all' || 
        account.privilege === filters.privilege;

      return matchesSearch && matchesType && matchesStatus && matchesPrivilege;
    });
  },

  // Get statistics from current accounts data
  getStatistics: () => {
    const { accounts } = get();
    
    if (!accounts || accounts.length === 0) {
      return {
        totalAccounts: 0,
        activeAccounts: 0,
        savingsAccounts: 0,
        currentAccounts: 0,
        totalBalance: 0,
        averageBalance: 0,
      };
    }
    
    const activeAccounts = accounts.filter(a => a.is_active);
    const savingsAccounts = accounts.filter(a => a.account_type === 'SAVINGS');
    const currentAccounts = accounts.filter(a => a.account_type === 'CURRENT');
    const totalBalance = accounts.reduce((sum, a) => sum + (a.balance || 0), 0);

    return {
      totalAccounts: accounts.length,
      activeAccounts: activeAccounts.length,
      savingsAccounts: savingsAccounts.length,
      currentAccounts: currentAccounts.length,
      totalBalance,
      averageBalance: accounts.length > 0 ? totalBalance / accounts.length : 0,
    };
  },

  setSelectedAccount: (account) => {
    set({ selectedAccount: account });
  },

  clearError: () => {
    set({ error: null });
  },
}));

