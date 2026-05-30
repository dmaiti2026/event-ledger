package com.dmaiti.eventledger.account;

import com.dmaiti.eventledger.account.dto.TransactionRequest;
import com.dmaiti.eventledger.account.model.TransactionType;
import com.dmaiti.eventledger.account.repository.AccountRepository;
import com.dmaiti.eventledger.account.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void missingEventId_returns400() {
        TransactionRequest request = validRequest();
        request.setEventId(null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("eventId");
    }

    @Test
    void blankEventId_returns400() {
        TransactionRequest request = validRequest();
        request.setEventId("  ");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("eventId");
    }

    @Test
    void missingType_returns400() {
        TransactionRequest request = validRequest();
        request.setType(null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("type");
    }

    @Test
    void negativeAmount_returns400() {
        TransactionRequest request = validRequest();
        request.setAmount(new BigDecimal("-100.00"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("amount");
    }

    @Test
    void zeroAmount_returns400() {
        TransactionRequest request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("amount");
    }

    @Test
    void missingCurrency_returns400() {
        TransactionRequest request = validRequest();
        request.setCurrency(null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("currency");
    }

    @Test
    void missingEventTimestamp_returns400() {
        TransactionRequest request = validRequest();
        request.setEventTimestamp(null);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("eventTimestamp");
    }

    @Test
    void accountNotFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/accounts/acct-nonexistent", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Account not found");
    }

    @Test
    void validRequest_returns201() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/accounts/acct-123/transactions", validRequest(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private TransactionRequest validRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setEventId("evt-valid");
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-05-10T10:00:00Z"));
        return request;
    }
}
