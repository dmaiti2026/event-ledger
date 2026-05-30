package com.dmaiti.eventledger.gateway.dto;

import com.dmaiti.eventledger.gateway.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTransactionRequest {

    private String eventId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private Instant eventTimestamp;

    public AccountTransactionRequest(String eventId, TransactionType type, BigDecimal amount,
                                     String currency, Instant eventTimestamp) {
        this.eventId = eventId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.eventTimestamp = eventTimestamp;
    }

    public String getEventId() { return eventId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getEventTimestamp() { return eventTimestamp; }
}
