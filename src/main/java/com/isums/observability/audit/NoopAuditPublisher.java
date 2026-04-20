package com.isums.observability.audit;

public class NoopAuditPublisher implements AuditPublisher {

    @Override
    public void publish(AuditEvent event) {
        // Intentionally empty. Services can depend on AuditPublisher before audit Kafka is enabled.
    }
}
