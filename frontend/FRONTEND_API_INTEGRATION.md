# Frontend-Backend API Integration Summary

## Overview

This document summarizes the API integration between the GDB Frontend and Backend services.

## Environment Configuration

All backend service URLs are configured in `.env`:

| Service | Environment Variable | Default Port |
|---------|---------------------|--------------|
| Auth | `VITE_AUTH_SERVICE_URL` | 8001 |
| Users | `VITE_USERS_SERVICE_URL` | 8002 |
| Accounts | `VITE_ACCOUNTS_SERVICE_URL` | 8003 |
| Transactions | `VITE_TRANSACTIONS_SERVICE_URL` | 8004 |
| Aadhar | `VITE_AADHAR_SERVICE_URL` | 8005 |
| Company CRV | `VITE_COMPANY_CRV_SERVICE_URL` | 8006 |
| Notification | `VITE_NOTIFICATION_SERVICE_URL` | 8007 |
| Payment Gateway | `VITE_PAYMENT_GATEWAY_URL` | 8008 |

## API Services (`src/services/api.js`)

### Axios Instances
Each service has its own axios instance with:
- Base URL from environment variable
- 10-15 second timeout
- Content-Type: application/json
- Auth token interceptor (for protected services)
- Response error interceptor (handles 401 for auto-logout)

### Service Methods

#### `authService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `login(credentials)` | POST `/api/v1/auth/login` | User login |
| `logout()` | POST `/api/v1/auth/logout` | User logout |
| `verifyToken()` | GET `/api/v1/auth/verify` | Verify JWT token |
| `register(userData)` | POST `/api/v1/auth/register` | Register new user |

#### `usersService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `getAll()` | GET `/api/v1/users` | Get all users |
| `getById(userId)` | GET `/api/v1/users/:id` | Get user by ID |
| `create(userData)` | POST `/api/v1/users` | Create user |
| `update(userId, data)` | PUT `/api/v1/users/:id` | Update user |
| `delete(userId)` | DELETE `/api/v1/users/:id` | Delete user |

#### `accountsService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `getAll()` | GET `/api/v1/accounts` | Get all accounts |
| `getByNumber(num)` | GET `/api/v1/accounts/:num` | Get account |
| `createSavings(data)` | POST `/api/v1/accounts/savings` | Create savings account |
| `createCurrent(data)` | POST `/api/v1/accounts/current` | Create current account |
| `update(num, data)` | PUT `/api/v1/accounts/:num` | Update account |
| `verifyPin(num, pin)` | POST `/api/v1/accounts/:num/verify-pin` | Verify PIN |

#### `transactionsService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `getAll()` | GET `/api/v1/transactions` | Get all transactions |
| `getByAccount(num)` | GET `/api/v1/transactions/account/:num` | Get account transactions |
| `deposit(account, amount, pin)` | POST `/api/v1/transactions/deposit` | Process deposit |
| `withdraw(account, amount, pin)` | POST `/api/v1/transactions/withdraw` | Process withdrawal |
| `transfer(from, to, amount, pin)` | POST `/api/v1/transactions/transfer` | Process transfer |

#### `aadharService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `verify(aadharNumber)` | GET `/api/v1/verify/:aadhar_number` | Verify Aadhar |
| `verifyWithDetails(data)` | POST `/api/v1/aadhar/verify` | Verify with full details |
| `getDetails(aadharNumber)` | GET `/api/v1/aadhar/:aadhar_number` | Get Aadhar details |

#### `companyCrvService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `verify(regNumber)` | GET `/api/v1/company/verify/:reg_number` | Verify company |
| `verifyWithDetails(data)` | POST `/api/v1/company/verify` | Verify with full details |
| `getDetails(regNumber)` | GET `/api/v1/company/:reg_number` | Get company details |

#### `paymentGatewayService`
| Method | API Endpoint | Description |
|--------|-------------|-------------|
| `validateTransfer(data)` | POST `/api/v1/payment/validate` | Validate transfer |
| `processPayment(data)` | POST `/api/v1/payment/process` | Process payment |
| `getStatus(txnId)` | GET `/api/v1/payment/status/:txnId` | Get payment status |
| `verifyCompletion(txnId)` | GET `/api/v1/payment/verify/:txnId` | Verify completion |

## Store Integration

### Configuration Flag
Each store has a `USE_REAL_API` flag:
```javascript
const USE_REAL_API = true;  // Set to true when backend is running
```

When `USE_REAL_API = true`:
- Stores call real backend APIs
- On failure, fallback to mock data

When `USE_REAL_API = false`:
- Stores use mock data only
- Simulated delays for realistic UX

### Store → Service Mapping

| Store | Service Used | Key Methods |
|-------|-------------|-------------|
| `authStore` | `authService` | login, logout, verifyToken |
| `userStore` | `usersService` | fetchUsers, createUser, updateUser, deleteUser |
| `accountStore` | `accountsService`, `aadharService`, `companyCrvService` | fetchAccounts, createSavingsAccount, createCurrentAccount, verifyPin, verifyAadhar, verifyCompanyRegistration |
| `transactionStore` | `transactionsService`, `paymentGatewayService` | fetchTransactions, processDeposit, processWithdrawal, processTransfer |

## Account Creation Flow

### Savings Account (Individual)
1. Frontend calls `verifyAadhar(aadharNumber)` → Aadhar Service
2. On success, user details are populated
3. Frontend calls `createSavingsAccount(accountData)` → Accounts Service

### Current Account (Business)
1. Frontend calls `verifyCompanyRegistration(regNumber)` → Company CRV Service
2. On success, company details are populated
3. Frontend calls `createCurrentAccount(accountData)` → Accounts Service

## Transfer Flow (with Payment Gateway Validation)

1. Frontend calls `processTransfer(from, to, amount, pin)` → Transactions Service
2. On success, calls `paymentGatewayService.validateTransfer()` for second validation
3. On success, transaction is completed

## Error Handling

- All service methods throw errors with meaningful messages
- Stores catch errors and set `error` state
- Mock fallback is available when backend is unavailable

## Authentication

- JWT tokens stored in `localStorage`
- Token automatically added to requests via interceptor
- 401 responses trigger automatic logout and redirect to login

## Testing Configuration

To test with mock data only:
```javascript
// In each store file:
const USE_REAL_API = false;
```

To test with real backend:
```javascript
// In each store file:
const USE_REAL_API = true;
```

## Required Backend Services

Make sure all backend services are running:
```bash
# Start all services (from project root)
docker-compose up -d

# Or start individually:
cd auth_service && uvicorn app.main:app --port 8001 --reload
cd users_service && uvicorn app.main:app --port 8002 --reload
cd accounts_service && uvicorn app.main:app --port 8003 --reload
cd transactions_service && uvicorn app.main:app --port 8004 --reload
cd aadhar_service && uvicorn app.main:app --port 8005 --reload
cd company_crv_service && uvicorn app.main:app --port 8006 --reload
cd notification_service && uvicorn app.main:app --port 8007 --reload
cd central_payment_gateway_service && uvicorn app.main:app --port 8008 --reload
```
