ALTER TABLE transfer_limits
ADD COLUMN IF NOT EXISTS transaction_limit INT DEFAULT 10;

-- Update existing limits based on privilege
UPDATE transfer_limits SET transaction_limit = 100 WHERE privilege = 'PREMIUM';
UPDATE transfer_limits SET transaction_limit = 50 WHERE privilege = 'GOLD';
UPDATE transfer_limits SET transaction_limit = 10 WHERE privilege = 'SILVER';
