package com.dmaiti.eventledger.account.service;

import com.dmaiti.eventledger.account.dto.AccountResponse;
import com.dmaiti.eventledger.account.dto.BalanceResponse;
import com.dmaiti.eventledger.account.dto.TransactionRequest;
import com.dmaiti.eventledger.account.dto.TransactionResponse;
import com.dmaiti.eventledger.account.entity.Account;
import com.dmaiti.eventledger.account.entity.Transaction;
import com.dmaiti.eventledger.account.model.ServiceConstants;
import com.dmaiti.eventledger.account.model.TransactionType;
import com.dmaiti.eventledger.account.repository.AccountRepository;
import com.dmaiti.eventledger.account.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse applyTransaction(String accountId, TransactionRequest request) {
        accountRepository.findById(accountId).orElseGet(() -> {
            Account account = new Account();
            account.setAccountId(accountId);
            return accountRepository.save(account);
        });

        Transaction transaction = new Transaction();
        transaction.setEventId(request.getEventId());
        transaction.setAccountId(accountId);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setEventTimestamp(request.getEventTimestamp());

        transaction = transactionRepository.save(transaction);
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountId) {
        BigDecimal credits = transactionRepository.sumByAccountIdAndType(accountId, TransactionType.CREDIT);
        BigDecimal debits = transactionRepository.sumByAccountIdAndType(accountId, TransactionType.DEBIT);
        long count = transactionRepository.countByAccountId(accountId);
        return new BalanceResponse(accountId, credits.subtract(debits), count);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(ServiceConstants.ERROR_ACCOUNT_NOT_FOUND + accountId));

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByEventTimestampAsc(accountId);
        BigDecimal credits = transactionRepository.sumByAccountIdAndType(accountId, TransactionType.CREDIT);
        BigDecimal debits = transactionRepository.sumByAccountIdAndType(accountId, TransactionType.DEBIT);

        List<TransactionResponse> txResponses = transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new AccountResponse(account.getAccountId(), account.getCreatedAt(), credits.subtract(debits), txResponses);
    }

    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse response = new TransactionResponse();
        response.setId(t.getId());
        response.setEventId(t.getEventId());
        response.setAccountId(t.getAccountId());
        response.setType(t.getType());
        response.setAmount(t.getAmount());
        response.setCurrency(t.getCurrency());
        response.setEventTimestamp(t.getEventTimestamp());
        response.setReceivedAt(t.getReceivedAt());
        return response;
    }
}
