package com.gdb.account.repository;

import com.gdb.account.domain.model.Account;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByAccountNumber(Long accountNumber);

    List<Account> findAll(String type, String privilege, Boolean isActive);

    void updateBalance(Long accountNumber, BigDecimal newBalance);

    void updateStatus(Long accountNumber, boolean isActive);

    boolean existsByNameAndDob(String name, String dob);

    boolean existsByRegistrationNo(String registrationNo);

    void saveSavingsDetails(Account account);

    void saveCurrentDetails(Account account);

    Long generateAccountNumber();
}
