package com.gdb.auth.service.impl;

import com.gdb.auth.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    private final RestTemplate restTemplate;

    @Value("${app.services.users-url}")
    private String usersServiceUrl;

    @Override
    public UserVerificationResponse verifyCredentials(String loginId, String password) {
        String url = usersServiceUrl + "/internal/v1/users/verify";
        try {
            Map<String, String> request = Map.of("loginId", loginId, "password", password);
            return restTemplate.postForObject(url, request, UserVerificationResponse.class);
        } catch (Exception e) {
            log.error("Error calling Users Service verifyCredentials: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public UserVerificationResponse getUserStatus(String loginId) {
        String url = usersServiceUrl + "/internal/v1/users/" + loginId + "/status";
        try {
            return restTemplate.getForObject(url, UserVerificationResponse.class);
        } catch (Exception e) {
            log.error("Error calling Users Service getUserStatus: {}", e.getMessage());
            return null;
        }
    }
}
