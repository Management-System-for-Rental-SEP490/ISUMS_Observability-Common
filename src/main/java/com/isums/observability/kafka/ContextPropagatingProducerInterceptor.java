package com.isums.observability.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public class ContextPropagatingProducerInterceptor implements ProducerInterceptor<Object, Object> {

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        putHeader(record, ObservabilityKafkaHeaders.REQUEST_ID, MDC.get("requestId"));
        putHeader(record, ObservabilityKafkaHeaders.CORRELATION_ID, MDC.get("correlationId"));
        putHeader(record, ObservabilityKafkaHeaders.ACTOR_USER_ID, MDC.get("userId"));
        putHeader(record, ObservabilityKafkaHeaders.ACTOR_ROLE, MDC.get("role"));

        String traceId = MDC.get("traceId");
        String spanId = MDC.get("spanId");
        if (StringUtils.hasText(traceId) && StringUtils.hasText(spanId)
                && record.headers().lastHeader(ObservabilityKafkaHeaders.TRACEPARENT) == null) {
            putHeader(record, ObservabilityKafkaHeaders.TRACEPARENT, "00-" + traceId + "-" + spanId + "-01");
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

    private void putHeader(ProducerRecord<Object, Object> record, String key, String value) {
        if (StringUtils.hasText(value) && record.headers().lastHeader(key) == null) {
            record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
