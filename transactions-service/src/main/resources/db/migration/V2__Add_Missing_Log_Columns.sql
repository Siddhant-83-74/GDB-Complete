-- Migration to add missing columns to transaction_logging as per requirements
ALTER TABLE transaction_logging 
ADD COLUMN IF NOT EXISTS reference_id BIGINT,
ADD COLUMN IF NOT EXISTS description VARCHAR(255),
ADD COLUMN IF NOT EXISTS mode VARCHAR(20),
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'SUCCESS';

-- Add constraint for status if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_transaction_status') THEN
        ALTER TABLE transaction_logging ADD CONSTRAINT chk_transaction_status CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING'));
    END IF;
END $$;
