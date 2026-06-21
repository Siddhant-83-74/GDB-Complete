package com.gdb.aadhar.service;

import com.gdb.aadhar.dto.AadharVerificationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Core service for Aadhar number verification.
 * Simulates UIDAI (Unique Identification Authority of India) verification
 * system.
 * Stateless — no database required. Valid numbers are hardcoded for testing.
 */
@Service
@Slf4j
public class AadharVerificationService {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AadharData {
        private String name;
        private String mobileNo;
        private String address;
        private String gender;
        private String dateOfBirth;
        private String photoUrl;
    }

    private static final Map<String, AadharData> AADHAR_DATABASE = Map.ofEntries(
            Map.entry("123456789012", AadharData.builder()
                    .name("Rajesh Kumar")
                    .mobileNo("9876543210")
                    .address("123, MG Road, Bangalore, Karnataka - 560001")
                    .gender("Male")
                    .dateOfBirth("1990-05-15")
                    .photoUrl("https://randomuser.me/api/portraits/men/1.jpg")
                    .build()),
            Map.entry("234567890123", AadharData.builder()
                    .name("Priya Sharma")
                    .mobileNo("9876543211")
                    .address("456, Park Street, Kolkata, West Bengal - 700016")
                    .gender("Female")
                    .dateOfBirth("1985-08-22")
                    .photoUrl("https://randomuser.me/api/portraits/women/2.jpg")
                    .build()),
            Map.entry("345678901234", AadharData.builder()
                    .name("Amit Patel")
                    .mobileNo("9876543212")
                    .address("789, SG Highway, Ahmedabad, Gujarat - 380015")
                    .gender("Male")
                    .dateOfBirth("1992-03-10")
                    .photoUrl("https://randomuser.me/api/portraits/men/3.jpg")
                    .build()),
            Map.entry("456789012345", AadharData.builder()
                    .name("Sneha Reddy")
                    .mobileNo("9876543213")
                    .address("321, Banjara Hills, Hyderabad, Telangana - 500034")
                    .gender("Female")
                    .dateOfBirth("1988-11-05")
                    .photoUrl("https://randomuser.me/api/portraits/women/4.jpg")
                    .build()),
            Map.entry("567890123456", AadharData.builder()
                    .name("Vikram Singh")
                    .mobileNo("9876543214")
                    .address("654, Connaught Place, New Delhi, Delhi - 110001")
                    .gender("Male")
                    .dateOfBirth("1995-01-20")
                    .photoUrl("https://randomuser.me/api/portraits/men/5.jpg")
                    .build()),
            Map.entry("678901234567", AadharData.builder()
                    .name("Anjali Mehta")
                    .mobileNo("9876543215")
                    .address("987, Marine Drive, Mumbai, Maharashtra - 400002")
                    .gender("Female")
                    .dateOfBirth("1991-07-18")
                    .photoUrl("https://randomuser.me/api/portraits/women/6.jpg")
                    .build()),
            Map.entry("789012345678", AadharData.builder()
                    .name("Karthik Iyer")
                    .mobileNo("9876543216")
                    .address("147, Anna Salai, Chennai, Tamil Nadu - 600002")
                    .gender("Male")
                    .dateOfBirth("1987-09-25")
                    .photoUrl("https://randomuser.me/api/portraits/men/7.jpg")
                    .build()),
            Map.entry("890123456789", AadharData.builder()
                    .name("Divya Nair")
                    .mobileNo("9876543217")
                    .address("258, MG Road, Kochi, Kerala - 682016")
                    .gender("Female")
                    .dateOfBirth("1993-04-12")
                    .photoUrl("https://randomuser.me/api/portraits/women/8.jpg")
                    .build()),
            Map.entry("901234567890", AadharData.builder()
                    .name("Arjun Desai")
                    .mobileNo("9876543218")
                    .address("369, FC Road, Pune, Maharashtra - 411004")
                    .gender("Male")
                    .dateOfBirth("1989-12-08")
                    .photoUrl("https://randomuser.me/api/portraits/men/9.jpg")
                    .build()),
            Map.entry("012345678901", AadharData.builder()
                    .name("Meera Kapoor")
                    .mobileNo("9876543219")
                    .address("741, Mall Road, Shimla, Himachal Pradesh - 171001")
                    .gender("Female")
                    .dateOfBirth("1994-06-30")
                    .photoUrl("https://randomuser.me/api/portraits/women/10.jpg")
                    .build()));

    /**
     * Masks an Aadhar number for logging, showing only the first 4 digits.
     * Example: "123456789012" → "1234XXXXXXXX"
     */
    private String maskAadhar(String aadharNumber) {
        if (aadharNumber == null || aadharNumber.length() < 4) {
            return "XXXXXXXXXXXX";
        }
        return aadharNumber.substring(0, 4) + "XXXXXXXX";
    }

    /**
     * Validates that the Aadhar number format is correct.
     *
     * @param aadharNumber the Aadhar number to validate
     * @return null if valid, or an error message string if invalid
     */
    public String validateFormat(String aadharNumber) {
        if (aadharNumber == null || aadharNumber.isBlank()) {
            return "Aadhar number is required";
        }

        // Check for non-numeric characters
        if (!aadharNumber.matches("\\d+")) {
            return "Aadhar number must contain only numeric characters";
        }

        // Check exact length
        if (aadharNumber.length() != 12) {
            return "Aadhar number must be exactly 12 digits";
        }

        return null; // Format is valid
    }

    /**
     * Verifies an Aadhar number against the valid numbers database.
     *
     * @param aadharNumber the Aadhar number to verify (must pass format validation
     *                     first)
     * @return verification response with status and message
     */
    public AadharVerificationResponse verify(String aadharNumber) {
        log.info("Verifying Aadhar number: {}", maskAadhar(aadharNumber));

        AadharData data = AADHAR_DATABASE.get(aadharNumber);

        if (data != null) {
            log.info("Aadhar number {} is VERIFIED", maskAadhar(aadharNumber));
            return AadharVerificationResponse.builder()
                    .aadharNumber(aadharNumber)
                    .isValid(true)
                    .name(data.getName())
                    .mobileNo(data.getMobileNo())
                    .address(data.getAddress())
                    .gender(data.getGender())
                    .dateOfBirth(data.getDateOfBirth())
                    .photoUrl(data.getPhotoUrl())
                    .status("VERIFIED")
                    .message("Aadhar number verified successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            log.info("Aadhar number {} is INVALID", maskAadhar(aadharNumber));
            return AadharVerificationResponse.builder()
                    .aadharNumber(aadharNumber)
                    .isValid(false)
                    .status("INVALID")
                    .message("Aadhar number not found in UIDAI database")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Returns the list of valid Aadhar numbers for testing purposes.
     * Numbers are returned sorted for consistency.
     *
     * @return sorted list of valid Aadhar numbers
     */
    public List<String> getValidAadharNumbers() {
        List<String> numbers = new java.util.ArrayList<>(AADHAR_DATABASE.keySet());
        Collections.sort(numbers);
        return numbers;
    }
}
