package com.isums.observability.audit;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.isums.observability.context.AuditContext;

public class AuditEvent {

    private UUID eventId = UUID.randomUUID();
    private int eventVersion = 1;
    private String traceId;
    private String spanId;
    private String requestId;
    private String correlationId;
    private String actorUserId;
    private String actorUsername;
    private String actorRole;
    private String actorType;
    private String tenantId;
    private String houseId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String serviceName;
    private AuditStatus status = AuditStatus.SUCCESS;
    private String clientIp;
    private String sourceIp;
    private String userAgent;
    private String cloudflareRayId;
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private String errorCode;
    private String errorMessage;
    private String idempotencyKey;
    private OffsetDateTime occurredAt = OffsetDateTime.now(ZoneOffset.UTC);
    private OffsetDateTime ingestedAt;
    private OffsetDateTime createdAt = occurredAt;

    public static AuditEvent fromContext(AuditContext context) {
        AuditEvent event = new AuditEvent();
        event.setTraceId(context.getTraceId());
        event.setSpanId(context.getSpanId());
        event.setRequestId(context.getRequestId());
        event.setCorrelationId(context.getCorrelationId());
        event.setActorUserId(context.getActorUserId());
        event.setActorUsername(context.getActorUsername());
        event.setActorRole(context.getActorRole());
        event.setActorType(context.getActorType());
        event.setTenantId(context.getTenantId());
        event.setHouseId(context.getHouseId());
        event.setClientIp(context.getClientIp());
        event.setSourceIp(context.getSourceIp());
        event.setUserAgent(context.getUserAgent());
        event.setCloudflareRayId(context.getCloudflareRayId());
        return event;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(int eventVersion) {
        this.eventVersion = eventVersion;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(String actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public void setStatus(AuditStatus status) {
        this.status = status;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCloudflareRayId() {
        return cloudflareRayId;
    }

    public void setCloudflareRayId(String cloudflareRayId) {
        this.cloudflareRayId = cloudflareRayId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : metadata;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public OffsetDateTime getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(OffsetDateTime ingestedAt) {
        this.ingestedAt = ingestedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
