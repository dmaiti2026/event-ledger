package com.dmaiti.eventledger.gateway.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public void increment(String metric) {
        counters.computeIfAbsent(metric, k -> new AtomicLong(0)).incrementAndGet();
    }

    public Map<String, Long> getAll() {
        Map<String, Long> snapshot = new LinkedHashMap<>();
        counters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> snapshot.put(e.getKey(), e.getValue().get()));
        return snapshot;
    }
}
