package com.dmaiti.eventledger.account.dto;

import com.dmaiti.eventledger.account.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionRequest {

    private String eventId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private Instant eventTimestamp;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}
