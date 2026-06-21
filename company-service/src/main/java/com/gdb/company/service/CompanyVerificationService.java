package com.gdb.company.service;

import com.gdb.company.dto.CompanyVerificationResponse;
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
 * Service for verifying Company Identification Numbers (CIN).
 * Simulates MCA/ROC verification.
 */
@Service
@Slf4j
public class CompanyVerificationService {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CompanyData {
        private String companyName;
        private String type;
        private String address;
        private String email;
        private String phone;
        private String website;
        private String incorporationDate;
        private String paidUpCapital;
        private List<String> directors;
    }

    private static final Map<String, CompanyData> COMPANY_DATABASE = Map.ofEntries(
            Map.entry("U72900KA2020PTC123456", CompanyData.builder()
                    .companyName("Tech Innovations Pvt Ltd")
                    .type("Private Limited")
                    .address("123 Tech Park, Electronic City, Bangalore, Karnataka - 560100")
                    .email("info@techinnovations.com")
                    .phone("+91-80-12345678")
                    .website("https://techinnovations.com")
                    .incorporationDate("2020-01-15")
                    .paidUpCapital("10,00,000")
                    .directors(List.of("Rajesh Kumar", "Priya Sharma"))
                    .build()),
            Map.entry("L67120MH2019PLC234567", CompanyData.builder()
                    .companyName("Global Finance Ltd")
                    .type("Public Limited")
                    .address("456 Financial District, BKC, Mumbai, Maharashtra - 400051")
                    .email("contact@globalfinance.com")
                    .phone("+91-22-23456789")
                    .website("https://globalfinance.com")
                    .incorporationDate("2019-06-20")
                    .paidUpCapital("50,00,000")
                    .directors(List.of("Suresh Menon", "Anita Desai", "Vikram Singh"))
                    .build()),
            Map.entry("U74999DL2021PTC345678", CompanyData.builder()
                    .companyName("Digital Solutions Pvt Ltd")
                    .type("Private Limited")
                    .address("789 Cyber Hub, Gurugram, Delhi NCR - 122002")
                    .email("hello@digitalsolutions.in")
                    .phone("+91-124-3456789")
                    .website("https://digitalsolutions.in")
                    .incorporationDate("2021-03-10")
                    .paidUpCapital("25,00,000")
                    .directors(List.of("Amit Patel", "Sneha Reddy"))
                    .build()),
            Map.entry("L85110TN2018PLC456789", CompanyData.builder()
                    .companyName("Manufacturing Excellence Ltd")
                    .type("Public Limited")
                    .address("Plot 100, SIPCOT Industrial Park, Chennai, Tamil Nadu - 600058")
                    .email("info@manufacturingexcellence.com")
                    .phone("+91-44-45678901")
                    .website("https://manufacturingexcellence.com")
                    .incorporationDate("2018-09-05")
                    .paidUpCapital("1,00,00,000")
                    .directors(List.of("Rahul Sharma", "Deepika Kapoor", "Manish Gupta"))
                    .build()),
            Map.entry("U51909GJ2022PTC567890", CompanyData.builder()
                    .companyName("Retail Ventures Pvt Ltd")
                    .type("Private Limited")
                    .address("55 Commerce Center, Ahmedabad, Gujarat - 380009")
                    .email("support@retailventures.co.in")
                    .phone("+91-79-56789012")
                    .website("https://retailventures.co.in")
                    .incorporationDate("2022-02-28")
                    .paidUpCapital("15,00,000")
                    .directors(List.of("Kiran Patel", "Meera Shah"))
                    .build()),
            Map.entry("L24233WB2017PLC678901", CompanyData.builder()
                    .companyName("Eastern Chemicals Ltd")
                    .type("Public Limited")
                    .address("Industrial Area, Durgapur, West Bengal - 713213")
                    .email("contact@easternchemicals.com")
                    .phone("+91-343-6789012")
                    .website("https://easternchemicals.com")
                    .incorporationDate("2017-11-15")
                    .paidUpCapital("75,00,000")
                    .directors(List.of("Arun Banerjee", "Suman Roy", "Priyanka Das"))
                    .build()),
            Map.entry("U45200HR2023PTC789012", CompanyData.builder()
                    .companyName("Green Energy Solutions Pvt Ltd")
                    .type("Private Limited")
                    .address("Eco Park, Sector 62, Faridabad, Haryana - 121004")
                    .email("info@greenenergysolutions.in")
                    .phone("+91-129-7890123")
                    .website("https://greenenergysolutions.in")
                    .incorporationDate("2023-01-20")
                    .paidUpCapital("30,00,000")
                    .directors(List.of("Vivek Tiwari", "Neha Agarwal"))
                    .build()),
            Map.entry("L29130AP2019PLC890123", CompanyData.builder()
                    .companyName("Pharma Health Ltd")
                    .type("Public Limited")
                    .address("Pharma City, Visakhapatnam, Andhra Pradesh - 530046")
                    .email("corporate@pharmahealth.com")
                    .phone("+91-891-8901234")
                    .website("https://pharmahealth.com")
                    .incorporationDate("2019-08-12")
                    .paidUpCapital("2,00,00,000")
                    .directors(List.of("Dr. Ramesh Naidu", "Dr. Lakshmi Devi", "Srinivas Rao"))
                    .build()),
            Map.entry("U62013RJ2021PTC901234", CompanyData.builder()
                    .companyName("Textile Creations Pvt Ltd")
                    .type("Private Limited")
                    .address("Textile Market, Jaipur, Rajasthan - 302001")
                    .email("sales@textilecreations.in")
                    .phone("+91-141-9012345")
                    .website("https://textilecreations.in")
                    .incorporationDate("2021-05-30")
                    .paidUpCapital("20,00,000")
                    .directors(List.of("Mahesh Jain", "Rekha Agarwal"))
                    .build()),
            Map.entry("L15142UP2020PLC012345", CompanyData.builder()
                    .companyName("Food Processing Industries Ltd")
                    .type("Public Limited")
                    .address("Food Park, Greater Noida, Uttar Pradesh - 201310")
                    .email("info@foodprocessing.com")
                    .phone("+91-120-0123456")
                    .website("https://foodprocessing.com")
                    .incorporationDate("2020-04-18")
                    .paidUpCapital("1,50,00,000")
                    .directors(List.of("Rajendra Singh", "Kavita Verma", "Alok Kumar"))
                    .build()));

    /**
     * Verifies a Company Identification Number (CIN).
     *
     * @param cin the 21-character CIN to verify
     * @return Verification response
     */
    public CompanyVerificationResponse verify(String cin) {
        log.info("Verifying Company Registration Number: {}", maskCin(cin));

        CompanyData data = COMPANY_DATABASE.get(cin);

        if (data != null) {
            log.info("CIN {} is VERIFIED", maskCin(cin));
            return CompanyVerificationResponse.builder()
                    .registrationNumber(cin)
                    .isValid(true)
                    .companyName(data.getCompanyName())
                    .type(data.getType())
                    .address(data.getAddress())
                    .email(data.getEmail())
                    .phone(data.getPhone())
                    .website(data.getWebsite())
                    .incorporationDate(data.getIncorporationDate())
                    .paidUpCapital(data.getPaidUpCapital())
                    .directors(data.getDirectors())
                    .status("VERIFIED")
                    .message("Company registration number verified successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            log.info("CIN {} is INVALID", maskCin(cin));
            return CompanyVerificationResponse.builder()
                    .registrationNumber(cin)
                    .isValid(false)
                    .status("INVALID")
                    .message("Company registration number not found in MCA records")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Masks CIN for logging (shows only first 8 characters).
     * Format: U72900KAXXXXXXXXXXXXX
     */
    private String maskCin(String cin) {
        if (cin == null || cin.length() < 8) {
            return "XXXXXXXXXXXXXXXXXXXXX";
        }
        return cin.substring(0, 8) + "XXXXXXXXXXXXX";
    }

    /**
     * Returns the list of valid CINs for testing.
     */
    public List<String> getValidCompanies() {
        List<String> validList = new java.util.ArrayList<>(COMPANY_DATABASE.keySet());
        Collections.sort(validList);
        return validList;
    }
}
