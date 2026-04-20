package com.isums.observability.http;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.isums.observability.audit.AuditEvent;
import com.isums.observability.audit.AuditPublisher;
import com.isums.observability.audit.AuditRequestAttributes;
import com.isums.observability.audit.AuditStatus;
import com.isums.observability.config.ObservabilityProperties;
import com.isums.observability.context.AuditContext;
import com.isums.observability.masking.SensitiveDataMasker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class HttpAuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpAuditFilter.class);
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final ObservabilityProperties properties;
    private final AuditPublisher auditPublisher;

    public HttpAuditFilter(ObservabilityProperties properties, AuditPublisher auditPublisher) {
        this.properties = properties;
        this.auditPublisher = auditPublisher;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !properties.getAudit().isEnabled()
                || properties.getHttp().getIgnoredPaths().stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long started = System.nanoTime();
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            if (request.getAttribute(AuditRequestAttributes.PUBLISHED) == null) {
                publishAuditEvent(request, response, started, failure);
            }
        }
    }

    private void publishAuditEvent(
            HttpServletRequest request,
            HttpServletResponse response,
            long started,
            Throwable failure) {
        try {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            int status = response.getStatus();
            AuditEvent event = AuditEvent.fromContext(AuditContext.currentOrMdc());
            event.setAction(resolveAction(request));
            event.setResourceType(resolveResourceType(request));
            event.setResourceId(resolveResourceId(request));
            event.setServiceName(properties.getServiceName());
            event.setStatus(failure == null && status < 400 ? AuditStatus.SUCCESS : AuditStatus.FAILURE);
            if (failure != null) {
                event.setErrorCode(failure.getClass().getSimpleName());
                event.setErrorMessage(SensitiveDataMasker.maskString(failure.getMessage()));
            } else if (status >= 400) {
                event.setErrorCode("HTTP_" + status);
            }
            event.setMetadata(metadata(request, status, durationMs));
            auditPublisher.publish(event);
            request.setAttribute(AuditRequestAttributes.PUBLISHED, Boolean.TRUE);
        } catch (Exception ex) {
            log.warn("Failed to publish HTTP audit event for {} {}", request.getMethod(), request.getRequestURI(), ex);
        }
    }

    private Map<String, Object> metadata(HttpServletRequest request, int status, long durationMs) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("method", request.getMethod());
        metadata.put("path", request.getRequestURI());
        if (StringUtils.hasText(request.getQueryString())) {
            metadata.put("query", SensitiveDataMasker.maskString(request.getQueryString()));
        }
        metadata.put("status", status);
        metadata.put("durationMs", durationMs);
        return metadata;
    }

    private String resolveAction(HttpServletRequest request) {
        return servicePrefix() + "." + operation(request);
    }

    private String operation(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        return switch (method) {
            case "GET" -> isResourceView(request) ? "VIEW" : "LIST";
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    private boolean isResourceView(HttpServletRequest request) {
        String resourceId = resolveResourceId(request);
        return StringUtils.hasText(resourceId);
    }

    private String resolveResourceType(HttpServletRequest request) {
        String[] segments = pathSegments(request);
        if (segments.length >= 2 && "api".equalsIgnoreCase(segments[0])) {
            return normalizeResourceType(segments[1]);
        }
        if (segments.length >= 1) {
            return normalizeResourceType(segments[0]);
        }
        return servicePrefix();
    }

    private String resolveResourceId(HttpServletRequest request) {
        String[] segments = pathSegments(request);
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            if (UUID_PATTERN.matcher(segment).matches()) {
                return segment;
            }
        }
        return null;
    }

    private String[] pathSegments(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return new String[0];
        }
        return java.util.Arrays.stream(path.split("/"))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }

    private String servicePrefix() {
        String serviceName = properties.getServiceName();
        if (!StringUtils.hasText(serviceName)) {
            return "UNKNOWN";
        }
        String normalized = serviceName.toUpperCase(Locale.ROOT)
                .replace("-SERVICE", "")
                .replace("_SERVICE", "");
        return normalizeToken(normalized);
    }

    private String normalizeResourceType(String value) {
        String normalized = value;
        if (normalized.endsWith("ies")) {
            normalized = normalized.substring(0, normalized.length() - 3) + "y";
        } else if (normalized.endsWith("s") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalizeToken(normalized);
    }

    private String normalizeToken(String value) {
        return value.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
