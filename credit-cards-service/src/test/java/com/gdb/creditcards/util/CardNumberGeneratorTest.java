package com.gdb.creditcards.util;

import com.gdb.creditcards.constants.CreditCardConstants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberGeneratorTest {

    @Test
    void generatesSixteenDigitsForEveryVendor() {
        for (String vendor : new String[] {
                CreditCardConstants.VENDOR_VISA,
                CreditCardConstants.VENDOR_MASTERCARD,
                CreditCardConstants.VENDOR_RUPAY }) {
            String pan = CardNumberGenerator.generate(vendor);
            assertThat(pan).matches("\\d{16}"); // b.1
            assertThat(CardNumberGenerator.isLuhnValid(pan)).isTrue();
        }
    }

    @Test
    void visaStartsWithFour() {
        assertThat(CardNumberGenerator.generate("VISA")).startsWith("4"); // b.11
    }

    @Test
    void mastercardStartsWith51to55() {
        String pan = CardNumberGenerator.generate("MASTERCARD");
        assertThat(pan.substring(0, 2)).matches("5[1-5]"); // b.11
    }

    @Test
    void rupayStartsWithKnownPrefix() {
        String pan = CardNumberGenerator.generate("RUPAY");
        assertThat(pan).matches("^(60|65|81).*");
    }

    @Test
    void rejectsLuhnInvalidNumber() {
        assertThat(CardNumberGenerator.isLuhnValid("4111111111111112")).isFalse();
        assertThat(CardNumberGenerator.isLuhnValid("12345")).isFalse();
    }
}
