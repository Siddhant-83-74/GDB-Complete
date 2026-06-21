package com.gdb.account.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

// MOD9-BUG-01 FIXED: @ExtendWith(MockitoExtension.class) initializes the @Mock fields.
// The client is built with an explicit URL because @Value is not processed in a plain unit test.
@ExtendWith(MockitoExtension.class)
public class AadharClientTest {

    @Mock
    private RestTemplate restTemplate;

    private AadharClient aadharClient;

    @BeforeEach
    void setUp() {
        aadharClient = new AadharClient(restTemplate, "http://localhost:8005");
    }

    @Test
    public void testVerifyAadhar_Success() {
        String testAadhar = "123456789012";
        String mockUrl = "http://localhost:8005/api/v1/verify";

        AadharClient.AadharVerificationResponse mockResponse = new AadharClient.AadharVerificationResponse(
                testAadhar, true, "SUCCESS", "Valid", "2026-06-16T12:00:00"
        );

        Mockito.when(restTemplate.postForObject(
                Mockito.eq(mockUrl),
                Mockito.anyMap(),
                Mockito.eq(AadharClient.AadharVerificationResponse.class)
        )).thenReturn(mockResponse);

        boolean result = aadharClient.verifyAadhar(testAadhar);
        assertTrue(result);
    }
}
