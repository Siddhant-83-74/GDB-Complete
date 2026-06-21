package com.gdb.account.service.impl;

import com.gdb.account.client.AadharClient;
import com.gdb.account.client.CompanyClient;
import com.gdb.account.domain.model.Account;
import com.gdb.account.dto.request.AccountOperationRequest;
import com.gdb.account.dto.request.SavingsAccountRequest;
import com.gdb.account.dto.request.CurrentAccountRequest;
import com.gdb.account.dto.response.AccountOperationResponse;
import com.gdb.account.dto.response.AccountResponse;
import com.gdb.account.exception.AccountException;
import com.gdb.account.constants.AccountConstants;
import com.gdb.account.factory.AccountFactory;
import com.gdb.account.mapper.AccountMapper;
import com.gdb.account.repository.AccountRepository;
import com.gdb.account.service.AccountService;
import com.gdb.account.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountFactory accountFactory;
    private final AadharClient aadharClient;
    private final CompanyClient companyClient;

    // TODO: MOD1-BUG-01: Injection point. Note that required=false prevents application startup crash.
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.gdb.account.util.AccountValidator accountValidator;

    @Override
    @Transactional
    public AccountResponse createSavingsAccount(SavingsAccountRequest request) {
        log.info("Creating savings account for: {}", request.getName());

        // TODO: MOD1-BUG-01: Fix NullPointerException. When this method is called,
        // it throws a NullPointerException because 'accountValidator' is null.
        // Identify why 'accountValidator' is not being injected by Spring.
        if (accountValidator != null) {
            accountValidator.validateSavingsOnboarding(request);
        } else {
            // Intentionally triggering a NullPointerException to simulate a dependency resolution failure.
            accountValidator.validateSavingsOnboarding(request);
        }

        // 1. Business Validations
        ValidationUtil.validatePin(request.getPin());
        ValidationUtil.validateAge(request.getDateOfBirth());

        // 2. Verify Aadhar number with external Aadhar Service
        log.info("Verifying Aadhar number with Aadhar Verification Service...");
        boolean aadharValid = aadharClient.verifyAadhar(request.getAadharNumber());
        if (!aadharValid) {
            throw new AccountException(
                    "Aadhar verification failed. The provided Aadhar number is not valid.",
                    "AADHAR_VERIFICATION_FAILED");
        }
        log.info("Aadhar number verified successfully");

        // 3. Check for duplicates
        if (accountRepository.existsByNameAndDob(request.getName(), request.getDateOfBirth())) {
            throw new AccountException("Account already exists for this name and DOB",
                    AccountConstants.ACCOUNT_ALREADY_EXISTS);
        }

        // 4. Hash PIN
        String pinHash = BCrypt.hashpw(request.getPin(), BCrypt.gensalt(AccountConstants.BCRYPT_SALT_ROUNDS));

        // 5. Generate Account Number (The "Method" requested)
        Long accountNumber = accountRepository.generateAccountNumber();

        // 6. Create Domain Object
        Account account = accountFactory.createSavingsAccount(request, pinHash, accountNumber);

        // 7. Persist (Atomic transaction)
        account = accountRepository.save(account);
        accountRepository.saveSavingsDetails(account);

        log.info("Savings account created successfully with number: {}", account.getAccountNumber());
        return AccountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse createCurrentAccount(CurrentAccountRequest request) {
        log.info("Creating current account for: {}", request.getCompanyName());

        // 1. Business Validations
        ValidationUtil.validatePin(request.getPin());

        // 2. Verify Company Registration (CRV)
        log.info("Verifying Company Registration Number with CRV Service...");
        boolean companyValid = companyClient.verifyCompany(request.getRegistrationNo());
        if (!companyValid) {
            throw new AccountException(
                    "Company verification failed. The provided Registration Number (CIN) is not valid.",
                    "COMPANY_VERIFICATION_FAILED");
        }
        log.info("Company Registration Number verified successfully");

        // 2. Check for duplicates
        if (accountRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new AccountException(
                    "Current account already exists for registration number: " + request.getRegistrationNo(),
                    AccountConstants.ACCOUNT_ALREADY_EXISTS);
        }

        // 3. Hash PIN
        String pinHash = BCrypt.hashpw(request.getPin(), BCrypt.gensalt(AccountConstants.BCRYPT_SALT_ROUNDS));

        // 4. Generate Account Number (The "Method" requested)
        Long accountNumber = accountRepository.generateAccountNumber();

        // 5. Create Domain Object
        Account account = accountFactory.createCurrentAccount(request, pinHash, accountNumber);

        // 6. Persist (Atomic transaction)
        account = accountRepository.save(account);
        accountRepository.saveCurrentDetails(account);

        log.info("Current account created successfully with number: {}", account.getAccountNumber());
        return AccountMapper.toResponse(account);
    }

    // TODO: MOD7-CR-01: Enable Caching.
    // Trainee task: Configure cache management by adding @EnableCaching to the main application 
    // or a configuration class.
    // 
    // TODO: MOD7-BUG-01: Stale data cache desync.
    // Trainee task: Notice that after you deposit or withdraw funds, the dashboard balance 
    // does not update immediately. The read query is cached but balance updates (debit/credit) 
    // do not evict or refresh the cache. Add the missing eviction annotation (@CacheEvict or @CachePut) 
    // to the write/modification methods.
    @Override
    @org.springframework.cache.annotation.Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponse getAccountByNumber(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found with number: " + accountNumber,
                        AccountConstants.ACCOUNT_NOT_FOUND));
        return AccountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAllAccounts(String type, String privilege, Boolean isActive) {
        List<Account> accounts = accountRepository.findAll(type, privilege, isActive);
        return AccountMapper.toResponseList(accounts);
    }

    // Internal API methods for service-to-service communication

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "accounts", key = "#request.accountNumber")
    public AccountOperationResponse debitAccount(AccountOperationRequest request) {
        log.info("Processing debit for account: {}, amount: {}", request.getAccountNumber(), request.getAmount());

        // 1. Validate and get account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountException("Account not found with number: " + request.getAccountNumber(),
                        AccountConstants.ACCOUNT_NOT_FOUND));

        // 2. Check if account is active
        if (!account.getIsActive()) {
            throw new AccountException("Account is not active", AccountConstants.ACCOUNT_NOT_ACTIVE);
        }

        // 3. Check sufficient balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AccountException("Insufficient balance", AccountConstants.INSUFFICIENT_BALANCE);
        }

        // 4. Store previous balance
        BigDecimal previousBalance = account.getBalance();

        // 5. Debit the amount
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);

        // 6. Update account
        accountRepository.updateBalance(account.getAccountNumber(), newBalance);

        log.info("Account {} debited successfully. Previous balance: {}, New balance: {}",
                request.getAccountNumber(), previousBalance, newBalance);

        return AccountOperationResponse.builder()
                .status("SUCCESS")
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .description(request.getDescription())
                .operationType("DEBIT")
                .timestamp(LocalDateTime.now())
                .idempotencyKey(request.getIdempotencyKey())
                .build();
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "accounts", key = "#request.accountNumber")
    public AccountOperationResponse creditAccount(AccountOperationRequest request) {
        log.info("Processing credit for account: {}, amount: {}", request.getAccountNumber(), request.getAmount());

        // 1. Validate and get account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountException("Account not found with number: " + request.getAccountNumber(),
                        AccountConstants.ACCOUNT_NOT_FOUND));

        // 2. Check if account is active
        if (!account.getIsActive()) {
            throw new AccountException("Account is not active", AccountConstants.ACCOUNT_NOT_ACTIVE);
        }

        // 3. Store previous balance
        BigDecimal previousBalance = account.getBalance();

        // 4. Credit the amount
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);

        // 5. Update account
        accountRepository.updateBalance(account.getAccountNumber(), newBalance);

        log.info("Account {} credited successfully. Previous balance: {}, New balance: {}",
                request.getAccountNumber(), previousBalance, newBalance);

        return AccountOperationResponse.builder()
                .status("SUCCESS")
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .description(request.getDescription())
                .operationType("CREDIT")
                .timestamp(LocalDateTime.now())
                .idempotencyKey(request.getIdempotencyKey())
                .build();
    }

    @Override
    public boolean isAccountActive(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found with number: " + accountNumber,
                        AccountConstants.ACCOUNT_NOT_FOUND));
        return account.getIsActive();
    }

    @Override
    public String getAccountPrivilege(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found with number: " + accountNumber,
                        AccountConstants.ACCOUNT_NOT_FOUND));
        return account.getPrivilege();
    }

    @Override
    public boolean verifyPin(Long accountNumber, String pin) {
        log.debug("Verifying PIN for account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found with number: " + accountNumber,
                        AccountConstants.ACCOUNT_NOT_FOUND));

        log.debug("Account found. PIN hash from DB: {}", account.getPinHash());
        log.debug("PIN provided: {}", pin);

        // jBCrypt only supports $2a$ prefix, but Python bcrypt uses $2b$.
        // They are cryptographically identical, so we safely normalize the prefix.
        String pinHash = account.getPinHash();
        if (pinHash != null && pinHash.startsWith("$2b$")) {
            pinHash = "$2a$" + pinHash.substring(4);
        }

        boolean result = BCrypt.checkpw(pin, pinHash);
        log.debug("PIN verification result: {}", result);

        return result;
    }
}
