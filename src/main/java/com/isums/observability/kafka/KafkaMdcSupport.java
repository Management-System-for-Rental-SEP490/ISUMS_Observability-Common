package com.isums.observability.kafka;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;

public final class KafkaMdcSupport {

    private KafkaMdcSupport() {
    }

    public static void copyHeadersToMdc(ConsumerRecord<?, ?> record) {
        putHeader(record, ObservabilityKafkaHeaders.REQUEST_ID, "requestId");
        putHeader(record, ObservabilityKafkaHeaders.CORRELATION_ID, "correlationId");
        putHeader(record, ObservabilityKafkaHeaders.ACTOR_USER_ID, "userId");
        putHeader(record, ObservabilityKafkaHeaders.ACTOR_ROLE, "role");
        putTraceparent(record);
        MDC.put("topic", record.topic());
        MDC.put("partition", String.valueOf(record.partition()));
        MDC.put("offset", String.valueOf(record.offset()));
    }

    private static void putHeader(ConsumerRecord<?, ?> record, String headerName, String mdcName) {
        Header header = record.headers().lastHeader(headerName);
        if (header != null && header.value() != null) {
            MDC.put(mdcName, new String(header.value(), StandardCharsets.UTF_8));
        }
    }

    private static void putTraceparent(ConsumerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(ObservabilityKafkaHeaders.TRACEPARENT);
        if (header == null || header.value() == null) {
            return;
        }
        String value = new String(header.value(), StandardCharsets.UTF_8);
        String[] parts = value.split("-");
        if (parts.length >= 4) {
            MDC.put("traceId", parts[1]);
            MDC.put("spanId", parts[2]);
        }
    }
}
