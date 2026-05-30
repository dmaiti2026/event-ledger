package com.dmaiti.eventledger.gateway.controller;

import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(GatewayConstants.HEALTH_KEY_SERVICE, GatewayConstants.SERVICE_NAME);

        try (Connection conn = dataSource.getConnection()) {
            response.put(GatewayConstants.HEALTH_KEY_STATUS, GatewayConstants.STATUS_UP);
            response.put(GatewayConstants.HEALTH_KEY_DATABASE, GatewayConstants.STATUS_UP);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(GatewayConstants.HEALTH_KEY_STATUS, GatewayConstants.STATUS_DOWN);
            response.put(GatewayConstants.HEALTH_KEY_DATABASE, GatewayConstants.STATUS_DOWN);
            return ResponseEntity.status(503).body(response);
        }
    }
}
