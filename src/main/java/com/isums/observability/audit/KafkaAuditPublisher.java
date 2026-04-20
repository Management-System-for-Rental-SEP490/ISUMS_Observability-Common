package com.isums.observability.audit;

import com.isums.observability.config.ObservabilityProperties;
import com.isums.observability.context.AuditContext;
import com.isums.observability.context.AuditContextHolder;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

public class KafkaAuditPublisher implements AuditPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObservabilityProperties properties;
    private final ObjectProvider<Tracer> tracerProvider;

    public KafkaAuditPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            ObservabilityProperties properties,
            ObjectProvider<Tracer> tracerProvider) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.tracerProvider = tracerProvider;
    }

    @Override
    public void publish(AuditEvent event) {
        enrich(event);
        String key = event.getActorUserId();
        if (!StringUtils.hasText(key) && event.getEventId() != null) {
            key = event.getEventId().toString();
        }
        kafkaTemplate.send(properties.getAudit().getTopic(), key, event);
    }

    private void enrich(AuditEvent event) {
        AuditContext context = AuditContextHolder.get();
        if (context == null) {
            context = AuditContext.fromMdc();
        }
        if (!StringUtils.hasText(event.getTraceId())) {
            event.setTraceId(context.getTraceId());
        }
        if (!StringUtils.hasText(event.getSpanId())) {
            event.setSpanId(context.getSpanId());
        }
        if (!StringUtils.hasText(event.getRequestId())) {
            event.setRequestId(context.getRequestId());
        }
        if (!StringUtils.hasText(event.getCorrelationId())) {
            event.setCorrelationId(context.getCorrelationId());
        }
        if (!StringUtils.hasText(event.getActorUserId())) {
            event.setActorUserId(context.getActorUserId());
        }
        if (!StringUtils.hasText(event.getActorUsername())) {
            event.setActorUsername(context.getActorUsername());
        }
        if (!StringUtils.hasText(event.getActorRole())) {
            event.setActorRole(context.getActorRole());
        }
        if (!StringUtils.hasText(event.getClientIp())) {
            event.setClientIp(context.getClientIp());
        }
        if (!StringUtils.hasText(event.getUserAgent())) {
            event.setUserAgent(context.getUserAgent());
        }
        if (!StringUtils.hasText(event.getCloudflareRayId())) {
            event.setCloudflareRayId(context.getCloudflareRayId());
        }
        if (!StringUtils.hasText(event.getServiceName())) {
            event.setServiceName(properties.getServiceName());
        }

        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                TraceContext traceContext = currentSpan.context();
                if (!StringUtils.hasText(event.getTraceId())) {
                    event.setTraceId(traceContext.traceId());
                }
                if (!StringUtils.hasText(event.getSpanId())) {
                    event.setSpanId(traceContext.spanId());
                }
                return;
            }
            CurrentTraceContext currentTraceContext = tracer.currentTraceContext();
            if (currentTraceContext != null && currentTraceContext.context() != null) {
                if (!StringUtils.hasText(event.getTraceId())) {
                    event.setTraceId(currentTraceContext.context().traceId());
                }
                if (!StringUtils.hasText(event.getSpanId())) {
                    event.setSpanId(currentTraceContext.context().spanId());
                }
            }
        }
    }
}
