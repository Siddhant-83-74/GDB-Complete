package com.gdb.account.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CompanyClient {

    private final RestTemplate restTemplate;
    private final String companyServiceUrl;

    public CompanyClient(RestTemplate restTemplate,
            @Value("${external.company-service.url}") String companyServiceUrl) {
        this.restTemplate = restTemplate;
        this.companyServiceUrl = companyServiceUrl;
    }

    public boolean verifyCompany(String registrationNumber) {
        String url = companyServiceUrl + "/api/v1/company/verify";
        log.info("Calling Company CRV Service at: {}", url);

        try {
            Map<String, String> request = new HashMap<>();
            request.put("registration_number", registrationNumber);

            CompanyVerificationResponse response = restTemplate.postForObject(
                    url,
                    request,
                    CompanyVerificationResponse.class);

            if (response != null) {
                log.info("Company verification response: {}", response);
                return response.isValid();
            } else {
                log.warn("Received null response from Company CRV Service");
                return false;
            }

        } catch (RestClientException e) {
            log.error("Error communicating with Company CRV Service", e);
            // In a real scenario, we might want to throw a specific service unavailable
            // exception
            // or return false depending on requirements. Assuming strict validation needed:
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyVerificationResponse {
        @JsonProperty("registration_number")
        private String registrationNumber;

        @JsonProperty("is_valid")
        private boolean isValid;

        private String status;
        private String message;
        private String timestamp;
    }
}
