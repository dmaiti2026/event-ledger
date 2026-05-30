package com.dmaiti.eventledger.gateway;

import com.dmaiti.eventledger.gateway.client.AccountServiceClient;
import com.dmaiti.eventledger.gateway.dto.EventRequest;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventGatewayValidationTest {

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
    void missingEventId_returns400() {
        EventRequest request = validRequest();
        request.setEventId(null);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("eventId");
    }

    @Test
    void blankEventId_returns400() {
        EventRequest request = validRequest();
        request.setEventId("  ");

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("eventId");
    }

    @Test
    void missingAccountId_returns400() {
        EventRequest request = validRequest();
        request.setAccountId(null);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("accountId");
    }

    @Test
    void missingType_returns400() {
        EventRequest request = validRequest();
        request.setType(null);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("type");
    }

    @Test
    void negativeAmount_returns400() {
        EventRequest request = validRequest();
        request.setAmount(new BigDecimal("-100.00"));

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("amount");
    }

    @Test
    void zeroAmount_returns400() {
        EventRequest request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("amount");
    }

    @Test
    void missingCurrency_returns400() {
        EventRequest request = validRequest();
        request.setCurrency(null);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("currency");
    }

    @Test
    void missingEventTimestamp_returns400() {
        EventRequest request = validRequest();
        request.setEventTimestamp(null);

        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("eventTimestamp");
    }

    @Test
    void eventNotFound_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/events/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Event not found");
    }

    @Test
    void validRequest_returns201() {
        ResponseEntity<String> response = restTemplate.postForEntity("/events", validRequest(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private EventRequest validRequest() {
        EventRequest request = new EventRequest();
        request.setEventId("evt-valid-" + System.nanoTime());
        request.setAccountId("acct-123");
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-05-10T10:00:00Z"));
        return request;
    }
}
