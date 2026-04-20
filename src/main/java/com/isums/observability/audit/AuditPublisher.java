package com.isums.observability.audit;

public interface AuditPublisher {

    void publish(AuditEvent event);
}
