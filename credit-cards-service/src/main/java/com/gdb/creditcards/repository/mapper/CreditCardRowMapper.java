package com.gdb.creditcards.repository.mapper;

import com.gdb.creditcards.domain.model.CreditCard;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CreditCardRowMapper implements RowMapper<CreditCard> {

    @Override
    public CreditCard mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CreditCard.builder()
                .id(rs.getString("id"))
                .userId(rs.getLong("user_id"))
                .cardHolderName(rs.getString("card_holder_name"))
                .mobileNumber(rs.getString("mobile_number"))
                .cardNumberEncrypted(rs.getString("card_number_encrypted"))
                .cardBin(rs.getString("card_bin"))
                .cardLast4(rs.getString("card_last4"))
                .vendor(rs.getString("vendor"))
                .category(rs.getString("category"))
                .cvvHash(rs.getString("cvv_hash"))
                .expiryDate(rs.getObject("expiry_date", java.time.LocalDate.class))
                .creditLimit(rs.getBigDecimal("credit_limit"))
                .availableCredit(rs.getBigDecimal("available_credit"))
                .outstandingAmount(rs.getBigDecimal("outstanding_amount"))
                .linkedAccountNumber(rs.getObject("linked_account_number") != null
                        ? rs.getLong("linked_account_number")
                        : null)
                .internationalEnabled(rs.getBoolean("international_enabled"))
                .status(rs.getString("status"))
                .consentSource(rs.getString("consent_source"))
                .otpVerified(rs.getBoolean("otp_verified"))
                .leadSource(rs.getString("lead_source"))
                .sourcingBranchCode(rs.getString("sourcing_branch_code"))
                .kycDocumentName(rs.getString("kyc_document_name"))
                .incomeDocumentName(rs.getString("income_document_name"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .updatedAt(rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime()
                        : null)
                .build();
    }
}
