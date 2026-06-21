package com.gdb.account.controller;

import com.gdb.account.domain.model.Account;
import com.gdb.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Test controller for debugging and creating test accounts.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final AccountRepository accountRepository;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/update-pin")
    public ResponseEntity<Map<String, Object>> updatePin(@RequestParam String pin) {
        try {
            // Generate BCrypt hash using the same logic as the application
            String pinHash = BCrypt.hashpw(pin, BCrypt.gensalt(12));
            log.info("Generated PIN hash for '{}': {}", pin, pinHash);
            
            // Use direct SQL update to change the PIN hash
            String sql = "UPDATE accounts SET pin_hash = ? WHERE account_number = 1000";
            int rowsUpdated = jdbcTemplate.update(sql, pinHash);
            
            if (rowsUpdated > 0) {
                return ResponseEntity.ok(Map.of(
                        "message", "PIN updated successfully",
                        "accountNumber", 1000,
                        "pin", pin,
                        "pinHash", pinHash,
                        "rowsUpdated", rowsUpdated
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "error", "No account found with number 1000"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error updating PIN", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to update PIN: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, Object>> testPinVerification(
            @RequestParam Long accountNumber, 
            @RequestParam String pin) {
        try {
            var account = accountRepository.findByAccountNumber(accountNumber);
            if (account.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "error", "Account not found"
                ));
            }
            
            String storedHash = account.get().getPinHash();
            boolean isValid = BCrypt.checkpw(pin, storedHash);
            
            return ResponseEntity.ok(Map.of(
                    "accountNumber", accountNumber,
                    "pinProvided", pin,
                    "storedHash", storedHash,
                    "isValid", isValid
            ));
            
        } catch (Exception e) {
            log.error("Error verifying PIN", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to verify PIN: " + e.getMessage()
            ));
        }
    }
}