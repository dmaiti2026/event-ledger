package com.dmaiti.eventledger.gateway.client;

import com.dmaiti.eventledger.gateway.dto.AccountTransactionRequest;
import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Value("${account-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "applyTransactionFallback")
    public boolean applyTransaction(String accountId, AccountTransactionRequest request) {
        String url = baseUrl + String.format(GatewayConstants.ACCOUNT_TRANSACTIONS_PATH, accountId);
        restTemplate.postForEntity(url, request, Object.class);
        return true;
    }

    public boolean applyTransactionFallback(String accountId, AccountTransactionRequest request, Throwable t) {
        log.warn("Account Service unavailable for accountId={} eventId={} — circuit breaker fallback: {}",
                accountId, request.getEventId(), t.getMessage());
        return false;
    }
}
