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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TracingAndMetricsTest {

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
    void everyResponse_hasTraceIdHeader() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(GatewayConstants.TRACE_ID_HEADER)).isNotBlank();
    }

    @Test
    void eachRequest_hasUniqueTraceId() {
        String traceId1 = restTemplate.getForEntity("/health", String.class)
                .getHeaders().getFirst(GatewayConstants.TRACE_ID_HEADER);
        String traceId2 = restTemplate.getForEntity("/health", String.class)
                .getHeaders().getFirst(GatewayConstants.TRACE_ID_HEADER);

        assertThat(traceId1).isNotBlank();
        assertThat(traceId2).isNotBlank();
        assertThat(traceId1).isNotEqualTo(traceId2);
    }

    @Test
    void postEvent_traceIdPresentInResponse() {
        EventRequest request = buildRequest();
        ResponseEntity<String> response = restTemplate.postForEntity("/events", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst(GatewayConstants.TRACE_ID_HEADER)).isNotBlank();
    }

    @Test
    void metricsEndpoint_returnsCounters() {
        restTemplate.postForEntity("/events", buildRequest(), String.class);
        restTemplate.getForEntity("/health", String.class);

        ResponseEntity<String> metrics = restTemplate.getForEntity("/metrics", String.class);

        assertThat(metrics.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metrics.getBody()).contains(GatewayConstants.METRIC_EVENTS_POST);
    }

    private EventRequest buildRequest() {
        EventRequest request = new EventRequest();
        request.setEventId("evt-trace-" + System.nanoTime());
        request.setAccountId("acct-123");
        request.setType(TransactionType.CREDIT);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-05-10T10:00:00Z"));
        return request;
    }
}
