package com.dmaiti.eventledger.gateway.model;

public final class GatewayConstants {

    private GatewayConstants() {}

    public static final String SERVICE_NAME            = "event-gateway";

    public static final String HEALTH_KEY_SERVICE      = "service";
    public static final String HEALTH_KEY_STATUS       = "status";
    public static final String HEALTH_KEY_DATABASE     = "database";

    public static final String STATUS_UP               = "UP";
    public static final String STATUS_DOWN             = "DOWN";

    public static final String TRACE_ID_HEADER           = "X-Trace-Id";
    public static final String MDC_TRACE_ID_KEY          = "traceId";

    public static final String METRIC_EVENTS_POST        = "events.post";
    public static final String METRIC_EVENTS_GET_BY_ID   = "events.get.by-id";
    public static final String METRIC_EVENTS_GET_BY_ACC  = "events.get.by-account";

    public static final String ERROR_EVENT_NOT_FOUND    = "Event not found: ";
    public static final String ERROR_VALIDATION_FAILED  = "Validation failed";
    public static final String ERROR_MALFORMED_REQUEST  = "Malformed or unreadable request body";
    public static final String ERROR_BAD_REQUEST        = "Bad Request";
    public static final String ERROR_NOT_FOUND          = "Not Found";
    public static final String ERROR_INTERNAL           = "Internal Server Error";

    public static final String ACCOUNT_TRANSACTIONS_PATH = "/accounts/%s/transactions";
}
