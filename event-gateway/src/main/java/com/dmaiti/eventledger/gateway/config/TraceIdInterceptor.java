package com.dmaiti.eventledger.gateway.config;

import com.dmaiti.eventledger.gateway.model.GatewayConstants;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private final Tracer tracer;

    public TraceIdInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Span span = tracer.currentSpan();
        if (span != null) {
            response.setHeader(GatewayConstants.TRACE_ID_HEADER, span.context().traceId());
        }
        return true;
    }
}
