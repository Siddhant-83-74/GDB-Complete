package com.gdb.creditcards.util;

import com.gdb.creditcards.exception.CreditCardException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class CardValidatorTest {

    private final LocalDate today = LocalDate.of(2026, 1, 1);

    @Test
    void cardNumberMustBe16Digits() { // b.1
        assertThatThrownBy(() -> CardValidator.validateCardNumber("12345"))
                .isInstanceOf(CreditCardException.class);
        assertThatCode(() -> CardValidator.validateCardNumber("4111111111111111"))
                .doesNotThrowAnyException();
    }

    @Test
    void cvvMustBe3Digits() { // b.4
        assertThatThrownBy(() -> CardValidator.validateCvv("12")).isInstanceOf(CreditCardException.class);
        assertThatThrownBy(() -> CardValidator.validateCvv("1234")).isInstanceOf(CreditCardException.class);
        assertThatCode(() -> CardValidator.validateCvv("123")).doesNotThrowAnyException();
    }

    @Test
    void mobileMustBe10Digits() { // b.5
        assertThatThrownBy(() -> CardValidator.validateMobile("99999")).isInstanceOf(CreditCardException.class);
        assertThatCode(() -> CardValidator.validateMobile("9876543210")).doesNotThrowAnyException();
    }

    @Test
    void nameIsMandatory() { // b.8
        assertThatThrownBy(() -> CardValidator.validateName("  ")).isInstanceOf(CreditCardException.class);
        assertThatCode(() -> CardValidator.validateName("Asha")).doesNotThrowAnyException();
    }

    @Test
    void expiryMustBeAtLeast3YearsOut() { // b.2
        assertThatThrownBy(() -> CardValidator.validateExpiry(today.plusYears(2), 3, today))
                .isInstanceOf(CreditCardException.class);
        assertThatCode(() -> CardValidator.validateExpiry(today.plusYears(3), 3, today))
                .doesNotThrowAnyException();
    }

    @Test
    void vendorMustBeSupported() { // b.10
        assertThatThrownBy(() -> CardValidator.validateVendor("AMEX")).isInstanceOf(CreditCardException.class);
        assertThatCode(() -> CardValidator.validateVendor("RUPAY")).doesNotThrowAnyException();
    }
}
