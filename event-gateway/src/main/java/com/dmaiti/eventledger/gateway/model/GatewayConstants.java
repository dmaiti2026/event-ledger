package com.dmaiti.eventledger.gateway.model;

public final class GatewayConstants {

    private GatewayConstants() {}

    public static final String SERVICE_NAME            = "event-gateway";

    public static final String HEALTH_KEY_SERVICE      = "service";
    public static final String HEALTH_KEY_STATUS       = "status";
    public static final String HEALTH_KEY_DATABASE     = "database";

    public static final String STATUS_UP               = "UP";
    public static final String STATUS_DOWN             = "DOWN";

    public static final String ERROR_EVENT_NOT_FOUND   = "Event not found: ";

    public static final String ACCOUNT_TRANSACTIONS_PATH = "/accounts/%s/transactions";
}
