package com.gdb.creditcards.service;

import com.gdb.creditcards.client.PaymentGatewayClient;
import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.domain.model.CreditCard;
import com.gdb.creditcards.dto.request.CardTransactionRequest;
import com.gdb.creditcards.dto.response.TransactionResponse;
import com.gdb.creditcards.exception.CreditCardException;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import com.gdb.creditcards.service.impl.CreditCardTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardTransactionServiceImplTest {

    @Mock
    private CreditCardRepository cardRepository;
    @Mock
    private CreditCardTransactionRepository txnRepository;
    @Mock
    private CreditCardService cardService;
    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @InjectMocks
    private CreditCardTransactionServiceImpl service;

    private static final String CARD_ID = "card-1";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "highValueThreshold", new BigDecimal("50000"));
        ReflectionTestUtils.setField(service, "velocityCount", 3);
        ReflectionTestUtils.setField(service, "velocityWindowMinutes", 5);
    }

    private CreditCard activeCard(BigDecimal available, boolean intl) {
        return CreditCard.builder()
                .id(CARD_ID)
                .status(CreditCardConstants.STATUS_ACTIVE)
                .creditLimit(new BigDecimal("100000"))
                .availableCredit(available)
                .outstandingAmount(BigDecimal.ZERO)
                .internationalEnabled(intl)
                .cardLast4("1234")
                .build();
    }

    private CardTransactionRequest req(BigDecimal amount, String channel, boolean intl) {
        CardTransactionRequest r = new CardTransactionRequest();
        r.setAmount(amount);
        r.setChannel(channel);
        r.setInternational(intl);
        return r;
    }

    @Test
    void approvesNormalTransactionAndDebits() {
        when(cardService.requireCard(CARD_ID)).thenReturn(activeCard(new BigDecimal("10000"), false));
        when(cardRepository.findServiceLimit(eq(CARD_ID), anyString())).thenReturn(Optional.empty());
        when(txnRepository.countSince(eq(CARD_ID), any(LocalDateTime.class))).thenReturn(0);

        TransactionResponse resp = service.transact(CARD_ID, req(new BigDecimal("2000"), "POS", false));

        assertThat(resp.getStatus()).isEqualTo(CreditCardConstants.TXN_APPROVED);
        verify(cardRepository).updateBalances(eq(CARD_ID), eq(new BigDecimal("8000")), eq(new BigDecimal("2000")));
    }

    @Test
    void rejectsWhenAmountExceedsAvailableCredit() { // b.3
        when(cardService.requireCard(CARD_ID)).thenReturn(activeCard(new BigDecimal("1000"), false));
        when(cardRepository.findServiceLimit(eq(CARD_ID), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transact(CARD_ID, req(new BigDecimal("5000"), "POS", false)))
                .isInstanceOf(CreditCardException.class)
                .hasFieldOrPropertyWithValue("errorCode", CreditCardConstants.LIMIT_EXCEEDED);
        verify(cardRepository, never()).updateBalances(anyString(), any(), any());
    }

    @Test
    void holdsHighValueTransaction() { // b.6
        when(cardService.requireCard(CARD_ID)).thenReturn(activeCard(new BigDecimal("100000"), false));
        when(cardRepository.findServiceLimit(eq(CARD_ID), anyString())).thenReturn(Optional.empty());

        TransactionResponse resp = service.transact(CARD_ID, req(new BigDecimal("60000"), "POS", false));

        assertThat(resp.getStatus()).isEqualTo(CreditCardConstants.TXN_ON_HOLD);
        assertThat(resp.getHoldReason()).contains("HIGH_VALUE");
        verify(cardRepository, never()).updateBalances(anyString(), any(), any());
    }

    @Test
    void holdsWhenVelocityExceeded() { // b.7
        when(cardService.requireCard(CARD_ID)).thenReturn(activeCard(new BigDecimal("100000"), false));
        when(cardRepository.findServiceLimit(eq(CARD_ID), anyString())).thenReturn(Optional.empty());
        when(txnRepository.countSince(eq(CARD_ID), any(LocalDateTime.class))).thenReturn(3);

        TransactionResponse resp = service.transact(CARD_ID, req(new BigDecimal("1000"), "POS", false));

        assertThat(resp.getStatus()).isEqualTo(CreditCardConstants.TXN_ON_HOLD);
        assertThat(resp.getHoldReason()).contains("VELOCITY");
    }

    @Test
    void rejectsInternationalWhenDisabled() { // b.16
        when(cardService.requireCard(CARD_ID)).thenReturn(activeCard(new BigDecimal("100000"), false));

        assertThatThrownBy(() -> service.transact(CARD_ID, req(new BigDecimal("1000"), "INTERNATIONAL", true)))
                .isInstanceOf(CreditCardException.class)
                .hasFieldOrPropertyWithValue("errorCode", CreditCardConstants.INTERNATIONAL_DISABLED);
    }
}
