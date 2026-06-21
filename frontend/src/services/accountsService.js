/**
 * Accounts Service
 * 
 * Handles all account management related API calls.
 * Includes creating, updating, and fetching bank accounts.
 */

import { accountsApi, API_BASE_URLS } from './apiConfig';

export const accountsService = {
  /**
   * Get all accounts
   * @returns {Promise<Array>} List of all accounts
   */
  getAll: async () => {
    try {
      console.log('Fetching accounts from:', `${API_BASE_URLS.accounts}/api/v1/accounts`);
      const token = localStorage.getItem('token');
      console.log('Auth token:', token ? 'Present' : 'Missing');
      const response = await accountsApi.get('/api/v1/accounts');
      console.log('Accounts response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Fetch accounts error:', error.response?.status, error.response?.data);
      throw new Error(error.response?.data?.message || error.response?.data?.detail || 'Failed to fetch accounts');
    }
  },

  /**
   * Get account by account number
   * @param {string|number} accountNumber - Account number
   * @returns {Promise<Object>} Account details
   */
  getByNumber: async (accountNumber) => {
    try {
      const response = await accountsApi.get(`/api/v1/accounts/${accountNumber}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Account not found');
    }
  },

  /**
   * Create savings account
   * @param {Object} accountData - Savings account data
   * @returns {Promise<Object>} Created account data
   */
  createSavings: async (accountData) => {
    try {
      const response = await accountsApi.post('/api/v1/accounts/savings', accountData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to create savings account');
    }
  },

  /**
   * Create current account
   * @param {Object} accountData - Current account data
   * @returns {Promise<Object>} Created account data
   */
  createCurrent: async (accountData) => {
    try {
      const response = await accountsApi.post('/api/v1/accounts/current', accountData);
      return response.data;
    } catch (error) {
      const errorDetail = error.response?.data?.detail || error.response?.data?.message;
      if (errorDetail?.includes('DUPLICATE') || errorDetail?.includes('already exists')) {
        throw new Error('A current account already exists for this company registration number');
      }
      throw new Error(errorDetail || 'Failed to create current account');
    }
  },

  /**
   * Update account
   * @param {string|number} accountNumber - Account number
   * @param {Object} accountData - Updated account data
   * @returns {Promise<Object>} Updated account data
   */
  update: async (accountNumber, accountData) => {
    try {
      const response = await accountsApi.put(`/api/v1/accounts/${accountNumber}`, accountData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to update account');
    }
  },

  /**
   * Verify account PIN
   * @param {string|number} accountNumber - Account number
   * @param {string} pin - PIN to verify
   * @returns {Promise<Object>} Verification result
   */
  verifyPin: async (accountNumber, pin) => {
    try {
      const response = await accountsApi.post(`/api/v1/accounts/${accountNumber}/verify-pin`, { pin });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'PIN verification failed');
    }
  },
};

export default accountsService;
