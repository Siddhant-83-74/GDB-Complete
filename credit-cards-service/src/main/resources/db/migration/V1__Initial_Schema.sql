-- Credit Cards core table.
-- Security (requirement f): the PAN is stored only as AES ciphertext
-- (card_number_encrypted); card_bin/card_last4 are kept for vendor logic and
-- masking. The CVV is stored only as a bcrypt hash (cvv_hash).
CREATE TABLE IF NOT EXISTS credit_cards (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_holder_name VARCHAR(255) NOT NULL,           -- b.8
    mobile_number VARCHAR(10) NOT NULL,               -- b.5
    card_number_encrypted TEXT NOT NULL,              -- b.1 / f
    card_bin VARCHAR(6) NOT NULL,                      -- b.11
    card_last4 VARCHAR(4) NOT NULL,                    -- b.12
    vendor VARCHAR(20) NOT NULL,                       -- b.10 VISA/MASTERCARD/RUPAY
    category VARCHAR(20) NOT NULL,                     -- b.13 SILVER/GOLD/PLATINUM
    cvv_hash VARCHAR(255) NOT NULL,                    -- b.4 / f
    expiry_date DATE NOT NULL,                         -- b.2
    credit_limit NUMERIC(15, 2) NOT NULL,             -- b.3 / b.13
    available_credit NUMERIC(15, 2) NOT NULL,
    outstanding_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    linked_account_number BIGINT,                      -- b.14 optional
    international_enabled BOOLEAN NOT NULL DEFAULT FALSE, -- b.16
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',      -- b.9 ACTIVE/INACTIVE/BLOCKED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vendor CHECK (vendor IN ('VISA', 'MASTERCARD', 'RUPAY')),
    CONSTRAINT chk_category CHECK (category IN ('SILVER', 'GOLD', 'PLATINUM')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);

-- b.15 Per-channel spend limits (ECOMMERCE, ATM, POS)
CREATE TABLE IF NOT EXISTS card_service_limits (
    id VARCHAR(36) PRIMARY KEY,
    card_id VARCHAR(36) NOT NULL REFERENCES credit_cards(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    per_txn_limit NUMERIC(15, 2) NOT NULL,
    CONSTRAINT uq_card_channel UNIQUE (card_id, channel)
);

-- Transactions: drives limit accounting (b.3), high-value holds (b.6),
-- velocity holds (b.7).
CREATE TABLE IF NOT EXISTS credit_card_transactions (
    id VARCHAR(36) PRIMARY KEY,
    card_id VARCHAR(36) NOT NULL REFERENCES credit_cards(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,        -- PURCHASE / PAYMENT / REFUND
    channel VARCHAR(20) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    merchant VARCHAR(255),
    international BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,      -- APPROVED / ON_HOLD / DECLINED / REVERSED
    hold_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cards_user ON credit_cards(user_id);
CREATE INDEX IF NOT EXISTS idx_txn_card ON credit_card_transactions(card_id);
CREATE INDEX IF NOT EXISTS idx_txn_card_time ON credit_card_transactions(card_id, created_at);
