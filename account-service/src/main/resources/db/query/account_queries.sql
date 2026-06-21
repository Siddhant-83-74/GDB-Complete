-- FIND_BY_ACCOUNT_NUMBER
SELECT *
FROM accounts
WHERE account_number = :accountNumber;
-- SAVE_ACCOUNT
INSERT INTO accounts (
        account_number,
        account_type,
        name,
        pin_hash,
        balance,
        privilege,
        bank_name,
        bank_branch,
        ifsc_code,
        is_active
    )
VALUES (
        :accountNumber,
        :accountType,
        :name,
        :pinHash,
        :balance,
        :privilege,
        :bankName,
        :bankBranch,
        :ifscCode,
        :isActive
    );
-- SAVE_SAVINGS_DETAILS
INSERT INTO savings_account_details (
        account_number,
        date_of_birth,
        gender,
        phone_no,
        aadhar_number
    )
VALUES (
        :accountNumber,
        CAST(:dob AS DATE),
        CAST(:gender AS public.gender_enum),
        :phoneNo,
        :aadharNumber
    );
-- SAVE_CURRENT_DETAILS
INSERT INTO current_account_details (
        account_number,
        company_name,
        website,
        registration_no
    )
VALUES (
        :accountNumber,
        :companyName,
        :website,
        :registrationNo
    );
-- LOAD_SAVINGS_DETAILS
SELECT *
FROM savings_account_details
WHERE account_number = :accountNumber;
-- LOAD_CURRENT_DETAILS
SELECT *
FROM current_account_details
WHERE account_number = :accountNumber;
-- UPDATE_BALANCE
UPDATE accounts
SET balance = :balance,
    updated_at = CURRENT_TIMESTAMP
WHERE account_number = :accountNumber;
-- UPDATE_STATUS
UPDATE accounts
SET is_active = :isActive,
    updated_at = CURRENT_TIMESTAMP
WHERE account_number = :accountNumber;
-- CHECK_DUPLICATE_SAVINGS
SELECT COUNT(*)
FROM accounts a
    JOIN savings_account_details s ON a.account_number = s.account_number
WHERE a.name = :name
    AND s.date_of_birth = CAST(:dob AS DATE);
-- CHECK_DUPLICATE_CURRENT
SELECT COUNT(*)
FROM current_account_details
WHERE registration_no = :registrationNo;
-- FIND_ALL_ACCOUNTS
SELECT *
FROM accounts
WHERE 1 = 1;
-- GET_NEXT_ACCOUNT_NUMBER
SELECT nextval('account_number_seq');