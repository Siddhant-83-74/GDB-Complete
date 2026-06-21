package com.gdb.account.factory;

import com.gdb.account.domain.model.Account;
import com.gdb.account.dto.request.SavingsAccountRequest;
import com.gdb.account.dto.request.CurrentAccountRequest;
import com.gdb.account.constants.AccountConstants;
import org.springframework.stereotype.Component;

/**
 * Factory for creating Account domain objects from requests.
 */
@Component
public class AccountFactory {

        public Account createSavingsAccount(SavingsAccountRequest request, String pinHash, Long accountNumber) {
                return Account.builder()
                                .accountNumber(accountNumber)
                                .accountType("SAVINGS")
                                .name(request.getName())
                                .pinHash(pinHash)
                                .balance(request.getInitialBalance())
                                .privilege(request.getPrivilege())
                                .bankName(AccountConstants.BANK_NAME_DEFAULT)
                                .bankBranch(AccountConstants.BANK_BRANCH_DEFAULT)
                                .ifscCode(AccountConstants.IFSC_CODE_DEFAULT)
                                .isActive(true)
                                .savingsDetails(Account.SavingsDetails.builder()
                                                .dateOfBirth(request.getDateOfBirth())
                                                .gender(request.getGender())
                                                .phoneNo(request.getPhoneNo())
                                                .aadharNumber(request.getAadharNumber())
                                                .build())
                                .build();
        }

        public Account createCurrentAccount(CurrentAccountRequest request, String pinHash, Long accountNumber) {
                return Account.builder()
                                .accountNumber(accountNumber)
                                .accountType("CURRENT")
                                .name(request.getName())
                                .pinHash(pinHash)
                                .balance(java.math.BigDecimal.ZERO)
                                .privilege(request.getPrivilege())
                                .bankName(AccountConstants.BANK_NAME_DEFAULT)
                                .bankBranch(AccountConstants.BANK_BRANCH_DEFAULT)
                                .ifscCode(AccountConstants.IFSC_CODE_DEFAULT)
                                .isActive(true)
                                .currentDetails(Account.CurrentDetails.builder()
                                                .companyName(request.getCompanyName())
                                                .website(request.getWebsite())
                                                .registrationNo(request.getRegistrationNo())
                                                .build())
                                .build();
        }
}
