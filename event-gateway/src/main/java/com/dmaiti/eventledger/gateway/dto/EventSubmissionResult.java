package com.dmaiti.eventledger.gateway.dto;

public class EventSubmissionResult {

    private final EventResponse event;
    private final boolean duplicate;

    public EventSubmissionResult(EventResponse event, boolean duplicate) {
        this.event = event;
        this.duplicate = duplicate;
    }

    public EventResponse getEvent() { return event; }
    public boolean isDuplicate() { return duplicate; }
}
