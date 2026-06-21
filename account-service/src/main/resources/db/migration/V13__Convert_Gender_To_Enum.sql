-- Migration to convert gender column from VARCHAR to ENUM, as requested by user.
-- Create the enum type if it doesn't exist
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_type
    WHERE typname = 'gender_enum'
) THEN CREATE TYPE gender_enum AS ENUM ('Male', 'Female', 'Others');
END IF;
END $$;
-- Alter the column to use the new enum type
-- We use a USING clause to cast existing VARCHAR data to the ENUM type
ALTER TABLE savings_account_details
ALTER COLUMN gender TYPE gender_enum USING gender::gender_enum;