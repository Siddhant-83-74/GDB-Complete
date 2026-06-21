package com.gdb.users.controller;

import com.gdb.users.domain.model.SecurityAlertLog;
import com.gdb.users.repository.SecurityAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for retrieving security alerts.
 * 
 * TODO: MOD4-BUG-01: Mapped Property Sort crash (PropertyReferenceException).
 * Trainee task: Notice that when you sort by date, the endpoint returns a 500 error:
 * "No property 'alert_date' found for type 'SecurityAlertLog'".
 * Identify why it is failing and fix the sorting field name in the PageRequest.
 */
@RestController
@RequestMapping("/api/v1/users/security-alerts")
@RequiredArgsConstructor
public class SecurityAlertController {

    private final SecurityAlertRepository alertRepository;

    @GetMapping
    public Page<SecurityAlertLog> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "alertDate") String sortBy) {

        // MOD4-BUG-01 FIXED: sort by the JPA entity property "alertDate", not the DB column "alert_date"
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return alertRepository.findAll(pageRequest);
    }
}
