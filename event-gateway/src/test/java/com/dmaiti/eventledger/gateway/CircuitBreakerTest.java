package com.dmaiti.eventledger.gateway;

import com.dmaiti.eventledger.gateway.client.AccountServiceClient;
import com.dmaiti.eventledger.gateway.dto.EventRequest;
import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import com.dmaiti.eventledger.gateway.model.TransactionType;
import com.dmaiti.eventledger.gateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CircuitBreakerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventRepository eventRepository;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void whenAccountServiceSucceeds_syncStatusIsSynced() {
        when(accountServiceClient.applyTransaction(anyString(), any())).thenReturn(true);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", buildRequest("evt-cb-ok"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains(GatewayConstants.SYNC_STATUS_SYNCED);
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    void whenAccountServiceFails_eventStillSaved_syncStatusIsPending() {
        when(accountServiceClient.applyTransaction(anyString(), any())).thenReturn(false);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", buildRequest("evt-cb-fail"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains(GatewayConstants.SYNC_STATUS_PENDING);
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    void whenAccountServiceFails_clientStillReturns201_notServerError() {
        when(accountServiceClient.applyTransaction(anyString(), any())).thenReturn(false);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", buildRequest("evt-cb-degraded"), String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private EventRequest buildRequest(String eventId) {
        EventRequest request = new EventRequest();
        request.setEventId(eventId);
        request.setAccountId("acct-cb-123");
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-05-10T10:00:00Z"));
        return request;
    }
}
