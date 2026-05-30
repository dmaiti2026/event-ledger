package com.dmaiti.eventledger.gateway.controller;

import com.dmaiti.eventledger.gateway.dto.EventRequest;
import com.dmaiti.eventledger.gateway.dto.EventResponse;
import com.dmaiti.eventledger.gateway.dto.EventSubmissionResult;
import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import com.dmaiti.eventledger.gateway.service.EventService;
import com.dmaiti.eventledger.gateway.service.MetricsService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;
    private final MetricsService metricsService;

    public EventController(EventService eventService, MetricsService metricsService) {
        this.eventService = eventService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> submitEvent(@Valid @RequestBody EventRequest request) {
        log.info("POST /events eventId={} accountId={}", request.getEventId(), request.getAccountId());
        metricsService.increment(GatewayConstants.METRIC_EVENTS_POST);
        EventSubmissionResult result = eventService.submitEvent(request);
        HttpStatus status = result.isDuplicate() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result.getEvent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        log.info("GET /events/{}", id);
        metricsService.increment(GatewayConstants.METRIC_EVENTS_GET_BY_ID);
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEventsByAccount(@RequestParam("account") String accountId) {
        log.info("GET /events?account={}", accountId);
        metricsService.increment(GatewayConstants.METRIC_EVENTS_GET_BY_ACC);
        return ResponseEntity.ok(eventService.getEventsByAccount(accountId));
    }
}
