-- Create sequence for account numbers starting from 1000
CREATE SEQUENCE IF NOT EXISTS account_number_seq START WITH 1000 INCREMENT BY 1;

-- Accounts Table: Core account information
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number BIGINT UNIQUE NOT NULL DEFAULT nextval('account_number_seq'),
    account_type VARCHAR(20) NOT NULL, -- SAVINGS, CURRENT
    name VARCHAR(255) NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    privilege VARCHAR(20) NOT NULL, -- SILVER, GOLD, PREMIUM
    bank_name VARCHAR(255) DEFAULT 'Global Digital Bank',
    bank_branch VARCHAR(255) DEFAULT 'Main Branch',
    ifsc_code VARCHAR(20) DEFAULT 'GDB0000001',
    is_active BOOLEAN DEFAULT TRUE,
    activated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Savings Account Details
CREATE TABLE IF NOT EXISTS savings_account_details (
    id BIGSERIAL PRIMARY KEY,
    account_number BIGINT UNIQUE NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL, -- Male, Female, Others
    phone_no VARCHAR(20) NOT NULL,
    aadhar_number VARCHAR(12) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Current Account Details
CREATE TABLE IF NOT EXISTS current_account_details (
    id BIGSERIAL PRIMARY KEY,
    account_number BIGINT UNIQUE NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    website VARCHAR(255),
    registration_no VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Idempotency Table for Debit/Credit operations
CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_accounts_number ON accounts(account_number);
CREATE INDEX idx_accounts_active ON accounts(is_active);
CREATE INDEX idx_savings_aadhar ON savings_account_details(aadhar_number);
CREATE INDEX idx_current_reg_no ON current_account_details(registration_no);
