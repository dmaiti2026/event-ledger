package com.dmaiti.eventledger.gateway.client;

import com.dmaiti.eventledger.gateway.dto.AccountTransactionRequest;
import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Value("${account-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void applyTransaction(String accountId, AccountTransactionRequest request) {
        String url = baseUrl + String.format(GatewayConstants.ACCOUNT_TRANSACTIONS_PATH, accountId);
        restTemplate.postForEntity(url, request, Object.class);
    }
}
