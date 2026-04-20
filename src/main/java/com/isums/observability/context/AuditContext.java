package com.isums.observability.context;

import java.util.Optional;

import org.slf4j.MDC;

public class AuditContext {

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
    private String clientIp;
    private String sourceIp;
    private String userAgent;
    private String cloudflareRayId;

    public static AuditContext fromMdc() {
        AuditContext context = new AuditContext();
        context.setTraceId(MDC.get("traceId"));
        context.setSpanId(MDC.get("spanId"));
        context.setRequestId(MDC.get("requestId"));
        context.setCorrelationId(MDC.get("correlationId"));
        context.setActorUserId(MDC.get("userId"));
        context.setActorUsername(MDC.get("username"));
        context.setActorRole(MDC.get("role"));
        context.setActorType(MDC.get("actorType"));
        context.setTenantId(MDC.get("tenantId"));
        context.setHouseId(MDC.get("houseId"));
        context.setClientIp(MDC.get("clientIp"));
        context.setSourceIp(MDC.get("sourceIp"));
        context.setUserAgent(MDC.get("userAgent"));
        context.setCloudflareRayId(MDC.get("cloudflareRayId"));
        return context;
    }

    public static AuditContext currentOrMdc() {
        return Optional.ofNullable(AuditContextHolder.get()).orElseGet(AuditContext::fromMdc);
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
}
