package com.dmaiti.eventledger.gateway.dto;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<String> fieldErrors;
    private Instant timestamp;

    public ErrorResponse(int status, String error, String message, List<String> fieldErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public List<String> getFieldErrors() { return fieldErrors; }
    public Instant getTimestamp() { return timestamp; }
}
