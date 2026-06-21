ALTER TABLE savings_account_details
ADD COLUMN IF NOT EXISTS aadhar_number VARCHAR(12) DEFAULT '000000000000' NOT NULL;
-- Create index if not exists (Postgres generic way involves distinct command or PL/pgSQL, but simpler is just:)
CREATE INDEX IF NOT EXISTS idx_savings_aadhar ON savings_account_details(aadhar_number);