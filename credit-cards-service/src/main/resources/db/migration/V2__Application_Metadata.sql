-- Admin-facing application metadata captured when an officer raises a credit
-- card application on behalf of an applicant. These columns record how consent
-- was obtained, the sourcing channel, and the supporting KYC/income documents.
ALTER TABLE credit_cards
    ADD COLUMN IF NOT EXISTS consent_source VARCHAR(120),          -- e.g. PHYSICAL_FORM,DIGITAL_SIGNATURE,VERBAL_OTP (comma-separated)
    ADD COLUMN IF NOT EXISTS otp_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS lead_source VARCHAR(40),              -- BRANCH / COLD_CALL / DIGITAL_CAMPAIGN
    ADD COLUMN IF NOT EXISTS sourcing_branch_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS kyc_document_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS income_document_name VARCHAR(255);
