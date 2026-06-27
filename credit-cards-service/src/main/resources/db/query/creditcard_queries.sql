-- SAVE_CARD
INSERT INTO credit_cards (
        id, user_id, card_holder_name, mobile_number,
        card_number_encrypted, card_bin, card_last4, vendor, category,
        cvv_hash, expiry_date, credit_limit, available_credit, outstanding_amount,
        linked_account_number, international_enabled, status,
        consent_source, otp_verified, lead_source, sourcing_branch_code,
        kyc_document_name, income_document_name
    )
VALUES (
        :id, :userId, :cardHolderName, :mobileNumber,
        :cardNumberEncrypted, :cardBin, :cardLast4, :vendor, :category,
        :cvvHash, :expiryDate, :creditLimit, :availableCredit, :outstandingAmount,
        :linkedAccountNumber, :internationalEnabled, :status,
        :consentSource, :otpVerified, :leadSource, :sourcingBranchCode,
        :kycDocumentName, :incomeDocumentName
    );
-- FIND_CARD_BY_ID
SELECT *
FROM credit_cards
WHERE id = :id;
-- FIND_CARDS_BY_USER
SELECT *
FROM credit_cards
WHERE user_id = :userId
ORDER BY created_at DESC;
-- FIND_ALL_CARDS
SELECT *
FROM credit_cards
ORDER BY created_at DESC;
-- UPDATE_CARD_BALANCES
UPDATE credit_cards
SET available_credit = :availableCredit,
    outstanding_amount = :outstandingAmount,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id;
-- UPDATE_CARD_STATUS
UPDATE credit_cards
SET status = :status,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id;
-- UPDATE_CARD_INTERNATIONAL
UPDATE credit_cards
SET international_enabled = :enabled,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id;
-- LINK_CARD_ACCOUNT
UPDATE credit_cards
SET linked_account_number = :accountNumber,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id;
-- UPSERT_SERVICE_LIMIT
INSERT INTO card_service_limits (id, card_id, channel, per_txn_limit)
VALUES (:id, :cardId, :channel, :perTxnLimit)
ON CONFLICT (card_id, channel)
DO UPDATE SET per_txn_limit = EXCLUDED.per_txn_limit;
-- FIND_SERVICE_LIMITS
SELECT *
FROM card_service_limits
WHERE card_id = :cardId;
-- FIND_SERVICE_LIMIT_BY_CHANNEL
SELECT *
FROM card_service_limits
WHERE card_id = :cardId
    AND channel = :channel;
-- SAVE_TRANSACTION
INSERT INTO credit_card_transactions (
        id, card_id, type, channel, amount, merchant, international, status, hold_reason
    )
VALUES (
        :id, :cardId, :type, :channel, :amount, :merchant, :international, :status, :holdReason
    );
-- FIND_TRANSACTION_BY_ID
SELECT *
FROM credit_card_transactions
WHERE id = :id;
-- FIND_TRANSACTIONS_BY_CARD
SELECT *
FROM credit_card_transactions
WHERE card_id = :cardId
ORDER BY created_at DESC;
-- UPDATE_TRANSACTION_STATUS
UPDATE credit_card_transactions
SET status = :status
WHERE id = :id;
-- COUNT_TRANSACTIONS_SINCE
SELECT COUNT(*)
FROM credit_card_transactions
WHERE card_id = :cardId
    AND created_at >= :since
    AND status <> 'DECLINED';
