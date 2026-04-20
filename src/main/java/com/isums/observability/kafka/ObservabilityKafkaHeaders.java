package com.isums.observability.kafka;

public final class ObservabilityKafkaHeaders {

    public static final String TRACEPARENT = "traceparent";
    public static final String TRACESTATE = "tracestate";
    public static final String BAGGAGE = "baggage";
    public static final String REQUEST_ID = "x-request-id";
    public static final String CORRELATION_ID = "x-correlation-id";
    public static final String ACTOR_USER_ID = "actor-user-id";
    public static final String ACTOR_ROLE = "actor-role";

    private ObservabilityKafkaHeaders() {
    }
}
