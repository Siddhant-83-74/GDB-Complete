package com.gdb.creditcards.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String id;

    @JsonProperty("card_id")
    private String cardId;

    private String type;
    private String channel;
    private BigDecimal amount;
    private String merchant;
    private boolean international;
    private String status;

    @JsonProperty("hold_reason")
    private String holdReason;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
