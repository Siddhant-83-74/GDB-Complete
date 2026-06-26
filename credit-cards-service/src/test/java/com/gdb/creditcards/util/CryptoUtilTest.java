package com.gdb.creditcards.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoUtilTest { // requirement f

    private final CryptoUtil crypto = new CryptoUtil("test-secret-key-for-unit-tests!!");

    @Test
    void encryptThenDecryptRoundTrips() {
        String pan = "4111111111111234";
        String encrypted = crypto.encrypt(pan);
        assertThat(encrypted).isNotEqualTo(pan);
        assertThat(crypto.decrypt(encrypted)).isEqualTo(pan);
    }

    @Test
    void ciphertextIsNonDeterministicPerCall() {
        String pan = "4111111111111234";
        assertThat(crypto.encrypt(pan)).isNotEqualTo(crypto.encrypt(pan));
    }
}
