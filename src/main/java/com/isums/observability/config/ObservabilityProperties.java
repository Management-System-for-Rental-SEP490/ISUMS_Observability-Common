package com.isums.observability.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "isums.observability")
public class ObservabilityProperties {

    private boolean enabled = true;
    private String serviceName = "unknown-service";
    private String environment = "dev";
    private String region = "local";
    private final Audit audit = new Audit();
    private final Http http = new Http();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Audit getAudit() {
        return audit;
    }

    public Http getHttp() {
        return http;
    }

    public static class Audit {
        private boolean enabled = false;
        private String topic = "audit.events";
        private String dlqTopic = "audit.events.dlq";
        private int producerRetries = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getDlqTopic() {
            return dlqTopic;
        }

        public void setDlqTopic(String dlqTopic) {
            this.dlqTopic = dlqTopic;
        }

        public int getProducerRetries() {
            return producerRetries;
        }

        public void setProducerRetries(int producerRetries) {
            this.producerRetries = producerRetries;
        }
    }

    public static class Http {
        private String requestIdHeader = "X-Request-ID";
        private String correlationIdHeader = "X-Correlation-ID";
        private boolean trustCloudflareHeaders = true;
        private List<String> ignoredPaths = new ArrayList<>(List.of(
                "/actuator",
                "/v3/api-docs",
                "/swagger-ui",
                "/swagger-ui.html",
                "/error"
        ));

        public String getRequestIdHeader() {
            return requestIdHeader;
        }

        public void setRequestIdHeader(String requestIdHeader) {
            this.requestIdHeader = requestIdHeader;
        }

        public String getCorrelationIdHeader() {
            return correlationIdHeader;
        }

        public void setCorrelationIdHeader(String correlationIdHeader) {
            this.correlationIdHeader = correlationIdHeader;
        }

        public boolean isTrustCloudflareHeaders() {
            return trustCloudflareHeaders;
        }

        public void setTrustCloudflareHeaders(boolean trustCloudflareHeaders) {
            this.trustCloudflareHeaders = trustCloudflareHeaders;
        }

        public List<String> getIgnoredPaths() {
            return ignoredPaths;
        }

        public void setIgnoredPaths(List<String> ignoredPaths) {
            this.ignoredPaths = ignoredPaths;
        }
    }
}
