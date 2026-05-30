package com.dmaiti.eventledger.gateway.service;

import com.dmaiti.eventledger.gateway.client.AccountServiceClient;
import com.dmaiti.eventledger.gateway.exception.EventNotFoundException;
import com.dmaiti.eventledger.gateway.dto.AccountTransactionRequest;
import com.dmaiti.eventledger.gateway.dto.EventRequest;
import com.dmaiti.eventledger.gateway.dto.EventResponse;
import com.dmaiti.eventledger.gateway.dto.EventSubmissionResult;
import com.dmaiti.eventledger.gateway.entity.Event;
import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import com.dmaiti.eventledger.gateway.repository.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final AccountServiceClient accountServiceClient;
    private final ObjectMapper objectMapper;

    public EventService(EventRepository eventRepository,
                        AccountServiceClient accountServiceClient,
                        ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.accountServiceClient = accountServiceClient;
        this.objectMapper = objectMapper;
    }

    public EventSubmissionResult submitEvent(EventRequest request) {
        Optional<Event> existing = eventRepository.findByEventId(request.getEventId());
        if (existing.isPresent()) {
            return new EventSubmissionResult(toResponse(existing.get()), true);
        }

        Event event = new Event();
        event.setEventId(request.getEventId());
        event.setAccountId(request.getAccountId());
        event.setType(request.getType());
        event.setAmount(request.getAmount());
        event.setCurrency(request.getCurrency());
        event.setEventTimestamp(request.getEventTimestamp());
        event.setMetadata(serializeMetadata(request.getMetadata()));
        event = eventRepository.save(event);

        boolean synced = accountServiceClient.applyTransaction(request.getAccountId(),
                new AccountTransactionRequest(
                        request.getEventId(),
                        request.getType(),
                        request.getAmount(),
                        request.getCurrency(),
                        request.getEventTimestamp()));

        EventResponse response = toResponse(event);
        response.setAccountSyncStatus(synced ? GatewayConstants.SYNC_STATUS_SYNCED : GatewayConstants.SYNC_STATUS_PENDING);
        return new EventSubmissionResult(response, false);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id) {
        return eventRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByAccount(String accountId) {
        return eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EventResponse toResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setEventId(event.getEventId());
        response.setAccountId(event.getAccountId());
        response.setType(event.getType());
        response.setAmount(event.getAmount());
        response.setCurrency(event.getCurrency());
        response.setEventTimestamp(event.getEventTimestamp());
        response.setMetadata(deserializeMetadata(event.getMetadata()));
        response.setReceivedAt(event.getReceivedAt());
        response.setAccountSyncStatus(GatewayConstants.SYNC_STATUS_SYNCED);
        return response;
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, Object> deserializeMetadata(String metadata) {
        if (metadata == null) return null;
        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
