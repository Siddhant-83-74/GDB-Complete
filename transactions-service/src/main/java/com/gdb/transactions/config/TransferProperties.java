package com.gdb.transactions.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * TODO: MOD5-BUG-01: Configuration Properties binding mismatch.
 * Trainee task: The daily max transfer limit is resolving to 0.00.
 * Identify why the value in application.yml is not binding to the field 'dailyMaxLimit'.
 * Hint: Look at the exact key in application.yml under 'banking.limits' vs the field name.
 */
@Component
@ConfigurationProperties(prefix = "banking.limits")
@Data
public class TransferProperties {
    // Injected Bug MOD5-BUG-01: In application.yml, the key is daily-max but the field is dailyMaxLimit
    private BigDecimal dailyMax = BigDecimal.ZERO;
}
