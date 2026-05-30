package com.dmaiti.eventledger.account.model;

public final class ServiceConstants {

    private ServiceConstants() {}

    public static final String SERVICE_NAME       = "account-service";

    public static final String HEALTH_KEY_SERVICE  = "service";
    public static final String HEALTH_KEY_STATUS   = "status";
    public static final String HEALTH_KEY_DATABASE = "database";

    public static final String STATUS_UP   = "UP";
    public static final String STATUS_DOWN = "DOWN";

    public static final String ERROR_ACCOUNT_NOT_FOUND = "Account not found: ";
}
