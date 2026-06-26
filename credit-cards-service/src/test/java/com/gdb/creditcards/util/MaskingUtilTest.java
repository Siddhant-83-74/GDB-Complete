package com.gdb.creditcards.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingUtilTest { // b.12

    @Test
    void masksAllButLastFour() {
        assertThat(MaskingUtil.mask("4111111111111234")).isEqualTo("**** **** **** 1234");
    }

    @Test
    void masksFromLast4() {
        assertThat(MaskingUtil.maskFromLast4("9876")).isEqualTo("**** **** **** 9876");
    }

    @Test
    void neverExposesMoreThanFourDigits() {
        String masked = MaskingUtil.mask("4111111111111234");
        assertThat(masked.replaceAll("[^0-9]", "")).hasSize(4);
    }
}
