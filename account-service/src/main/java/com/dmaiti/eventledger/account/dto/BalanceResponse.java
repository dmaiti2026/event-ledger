package com.dmaiti.eventledger.account.dto;

import java.math.BigDecimal;

public class BalanceResponse {

    private String accountId;
    private BigDecimal balance;
    private long transactionCount;

    public BalanceResponse(String accountId, BigDecimal balance, long transactionCount) {
        this.accountId = accountId;
        this.balance = balance;
        this.transactionCount = transactionCount;
    }

    public String getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
    public long getTransactionCount() { return transactionCount; }
}
