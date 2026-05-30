package com.dmaiti.eventledger.account.controller;

import com.dmaiti.eventledger.account.dto.AccountResponse;
import com.dmaiti.eventledger.account.dto.BalanceResponse;
import com.dmaiti.eventledger.account.dto.TransactionRequest;
import com.dmaiti.eventledger.account.dto.TransactionResponse;
import com.dmaiti.eventledger.account.model.ServiceConstants;
import com.dmaiti.eventledger.account.service.AccountService;
import com.dmaiti.eventledger.account.service.MetricsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final MetricsService metricsService;

    public AccountController(AccountService accountService, MetricsService metricsService) {
        this.accountService = accountService;
        this.metricsService = metricsService;
    }

    @PostMapping("/{accountId}/transactions")
    public ResponseEntity<TransactionResponse> applyTransaction(
            @PathVariable String accountId,
            @Valid @RequestBody TransactionRequest request) {
        log.info("POST /accounts/{}/transactions eventId={} type={}", accountId, request.getEventId(), request.getType());
        metricsService.increment(ServiceConstants.METRIC_TRANSACTIONS_POST);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.applyTransaction(accountId, request));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        log.info("GET /accounts/{}/balance", accountId);
        metricsService.increment(ServiceConstants.METRIC_BALANCE_GET);
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountId) {
        log.info("GET /accounts/{}", accountId);
        metricsService.increment(ServiceConstants.METRIC_ACCOUNT_GET);
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}
