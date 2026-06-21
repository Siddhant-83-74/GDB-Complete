package com.gdb.account.mapper;

import com.gdb.account.domain.model.Account;
import com.gdb.account.dto.response.AccountResponse;

import java.util.List;
import java.util.stream.Collectors;

public class AccountMapper {

    public static AccountResponse toResponse(Account account) {
        if (account == null)
            return null;

        AccountResponse.AccountResponseBuilder builder = AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .name(account.getName())
                .balance(account.getBalance())
                .privilege(account.getPrivilege())
                .bankName(account.getBankName())
                .bankBranch(account.getBankBranch())
                .ifscCode(account.getIfscCode())
                .isActive(account.getIsActive())
                .activatedDate(account.getActivatedDate());

        if (account.getSavingsDetails() != null) {
            builder.savingsDetails(AccountResponse.SavingsDetails.builder()
                    .dateOfBirth(account.getSavingsDetails().getDateOfBirth())
                    .gender(account.getSavingsDetails().getGender())
                    .phoneNo(account.getSavingsDetails().getPhoneNo())
                    .aadharNumber(account.getSavingsDetails().getAadharNumber())
                    .build());
        }

        if (account.getCurrentDetails() != null) {
            builder.companyName(account.getCurrentDetails().getCompanyName());
            builder.currentDetails(AccountResponse.CurrentDetails.builder()
                    .companyName(account.getCurrentDetails().getCompanyName())
                    .website(account.getCurrentDetails().getWebsite())
                    .registrationNo(account.getCurrentDetails().getRegistrationNo())
                    .build());
        }

        return builder.build();
    }

    public static List<AccountResponse> toResponseList(List<Account> accounts) {
        return accounts.stream()
                .map(AccountMapper::toResponse)
                .collect(Collectors.toList());
    }

    private AccountMapper() {
    }
}
