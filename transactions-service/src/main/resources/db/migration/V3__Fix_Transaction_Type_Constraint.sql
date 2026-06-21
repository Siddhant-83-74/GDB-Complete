-- Fix transaction_type constraint to match enum values
ALTER TABLE transaction_logging DROP CONSTRAINT IF EXISTS chk_transaction_type;
ALTER TABLE transaction_logging ADD CONSTRAINT chk_transaction_type 
    CHECK (transaction_type IN ('WITHDRAW', 'DEPOSIT', 'TRANSFER'));
