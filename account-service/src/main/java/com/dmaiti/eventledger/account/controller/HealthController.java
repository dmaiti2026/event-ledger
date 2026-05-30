package com.dmaiti.eventledger.account.controller;

import com.dmaiti.eventledger.account.model.ServiceConstants;
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
        response.put(ServiceConstants.HEALTH_KEY_SERVICE, ServiceConstants.SERVICE_NAME);

        try (Connection conn = dataSource.getConnection()) {
            response.put(ServiceConstants.HEALTH_KEY_STATUS, ServiceConstants.STATUS_UP);
            response.put(ServiceConstants.HEALTH_KEY_DATABASE, ServiceConstants.STATUS_UP);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put(ServiceConstants.HEALTH_KEY_STATUS, ServiceConstants.STATUS_DOWN);
            response.put(ServiceConstants.HEALTH_KEY_DATABASE, ServiceConstants.STATUS_DOWN);
            return ResponseEntity.status(503).body(response);
        }
    }
}
