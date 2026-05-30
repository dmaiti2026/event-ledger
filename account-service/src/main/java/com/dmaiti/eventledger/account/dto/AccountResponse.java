package com.dmaiti.eventledger.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AccountResponse {

    private String accountId;
    private Instant createdAt;
    private BigDecimal balance;
    private List<TransactionResponse> transactions;

    public AccountResponse(String accountId, Instant createdAt, BigDecimal balance, List<TransactionResponse> transactions) {
        this.accountId = accountId;
        this.createdAt = createdAt;
        this.balance = balance;
        this.transactions = transactions;
    }

    public String getAccountId() { return accountId; }
    public Instant getCreatedAt() { return createdAt; }
    public BigDecimal getBalance() { return balance; }
    public List<TransactionResponse> getTransactions() { return transactions; }
}
