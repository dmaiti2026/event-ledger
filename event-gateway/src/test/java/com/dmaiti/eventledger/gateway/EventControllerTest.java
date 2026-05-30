package com.dmaiti.eventledger.gateway;

import com.dmaiti.eventledger.gateway.client.AccountServiceClient;
import com.dmaiti.eventledger.gateway.dto.EventRequest;
import com.dmaiti.eventledger.gateway.dto.EventResponse;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerTest {

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
    void submitNewEvent_returns201() {
        ResponseEntity<EventResponse> response = submitEvent(
                "evt-001", "acct-123", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEventId()).isEqualTo("evt-001");
        assertThat(response.getBody().getAccountId()).isEqualTo("acct-123");
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    void idempotency_duplicateEventReturns200() {
        submitEvent("evt-dup", "acct-123", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");

        ResponseEntity<EventResponse> duplicate = submitEvent(
                "evt-dup", "acct-123", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(duplicate.getBody().getEventId()).isEqualTo("evt-dup");
    }

    @Test
    void idempotency_duplicateDoesNotCreateAnotherRecord() {
        submitEvent("evt-once", "acct-123", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");
        submitEvent("evt-once", "acct-123", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");
        submitEvent("evt-once", "acct-123", TransactionType.CREDIT, "500.00", "2026-05-10T10:00:00Z");

        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    void getEventById_returnsCorrectEvent() {
        ResponseEntity<EventResponse> created = submitEvent(
                "evt-get", "acct-123", TransactionType.CREDIT, "300.00", "2026-05-10T10:00:00Z");

        Long id = created.getBody().getId();
        ResponseEntity<EventResponse> response = restTemplate.getForEntity("/events/" + id, EventResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEventId()).isEqualTo("evt-get");
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void getEventsByAccount_orderedByEventTimestamp() {
        // Submit in non-chronological arrival order: May 12, May 8, May 10
        submitEvent("evt-oor1", "acct-oor", TransactionType.CREDIT, "500.00", "2026-05-12T10:00:00Z");
        submitEvent("evt-oor2", "acct-oor", TransactionType.CREDIT, "200.00", "2026-05-08T09:00:00Z");
        submitEvent("evt-oor3", "acct-oor", TransactionType.DEBIT,  "150.00", "2026-05-10T14:00:00Z");

        ResponseEntity<EventResponse[]> response = restTemplate.getForEntity(
                "/events?account=acct-oor", EventResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        EventResponse[] events = response.getBody();
        assertThat(events).hasSize(3);
        // Must be in chronological order: May 8 → May 10 → May 12
        assertThat(events[0].getEventTimestamp()).isEqualTo(Instant.parse("2026-05-08T09:00:00Z"));
        assertThat(events[1].getEventTimestamp()).isEqualTo(Instant.parse("2026-05-10T14:00:00Z"));
        assertThat(events[2].getEventTimestamp()).isEqualTo(Instant.parse("2026-05-12T10:00:00Z"));
    }

    @Test
    void getEventsByAccount_returnsEmptyListForUnknownAccount() {
        ResponseEntity<EventResponse[]> response = restTemplate.getForEntity(
                "/events?account=acct-unknown", EventResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void submitEvent_metadataStoredAndReturned() {
        EventRequest request = buildRequest("evt-meta", "acct-123", TransactionType.CREDIT,
                "100.00", "2026-05-10T10:00:00Z");
        request.setMetadata(Map.of("source", "mainframe-batch", "batchId", "B-9042"));

        ResponseEntity<EventResponse> response = restTemplate.postForEntity("/events", request, EventResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getMetadata()).containsEntry("source", "mainframe-batch");
        assertThat(response.getBody().getMetadata()).containsEntry("batchId", "B-9042");
    }

    @Test
    void health_returnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    private ResponseEntity<EventResponse> submitEvent(String eventId, String accountId,
                                                       TransactionType type, String amount, String timestamp) {
        return restTemplate.postForEntity("/events",
                buildRequest(eventId, accountId, type, amount, timestamp), EventResponse.class);
    }

    private EventRequest buildRequest(String eventId, String accountId,
                                       TransactionType type, String amount, String timestamp) {
        EventRequest request = new EventRequest();
        request.setEventId(eventId);
        request.setAccountId(accountId);
        request.setType(type);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse(timestamp));
        return request;
    }
}
