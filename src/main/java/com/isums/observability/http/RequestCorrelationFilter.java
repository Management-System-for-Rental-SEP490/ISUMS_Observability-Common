package com.isums.observability.http;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.isums.observability.config.ObservabilityProperties;
import com.isums.observability.context.AuditContext;
import com.isums.observability.context.AuditContextHolder;
import com.isums.observability.masking.SensitiveDataMasker;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("isums.http.access");

    private final ObservabilityProperties properties;
    private final ObjectProvider<Tracer> tracerProvider;

    public RequestCorrelationFilter(ObservabilityProperties properties, ObjectProvider<Tracer> tracerProvider) {
        this.properties = properties;
        this.tracerProvider = tracerProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return properties.getHttp().getIgnoredPaths().stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long started = System.nanoTime();
        String requestId = firstNonBlank(
                request.getHeader(properties.getHttp().getRequestIdHeader()),
                request.getHeader("X-Amzn-Trace-Id"),
                UUID.randomUUID().toString());
        String correlationId = firstNonBlank(
                request.getHeader(properties.getHttp().getCorrelationIdHeader()),
                requestId);

        try {
            putBaseMdc(request, requestId, correlationId);
            response.setHeader(properties.getHttp().getRequestIdHeader(), requestId);
            response.setHeader(properties.getHttp().getCorrelationIdHeader(), correlationId);

            AuditContext context = buildAuditContext(request, requestId, correlationId);
            AuditContextHolder.set(context);

            filterChain.doFilter(request, response);
        } finally {
            enrichUserMdc(request);
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("durationMs", String.valueOf(durationMs));
            log.info("http_request completed");
            AuditContextHolder.clear();
            MDC.clear();
        }
    }

    private void putBaseMdc(HttpServletRequest request, String requestId, String correlationId) {
        MDC.put("serviceName", properties.getServiceName());
        MDC.put("environment", properties.getEnvironment());
        MDC.put("region", properties.getRegion());
        MDC.put("requestId", requestId);
        MDC.put("correlationId", correlationId);
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getRequestURI());
        MDC.put("clientIp", resolveClientIp(request));
        MDC.put("sourceIp", request.getRemoteAddr());
        putIfPresent("userAgent", SensitiveDataMasker.maskString(request.getHeader("User-Agent")));
        putIfPresent("cloudflareRayId", request.getHeader("CF-Ray"));
        putTraceMdc();
        enrichUserMdc(request);
    }

    private AuditContext buildAuditContext(HttpServletRequest request, String requestId, String correlationId) {
        AuditContext context = AuditContext.fromMdc();
        context.setRequestId(requestId);
        context.setCorrelationId(correlationId);
        context.setClientIp(resolveClientIp(request));
        context.setSourceIp(request.getRemoteAddr());
        context.setUserAgent(SensitiveDataMasker.maskString(request.getHeader("User-Agent")));
        context.setCloudflareRayId(request.getHeader("CF-Ray"));
        return context;
    }

    private void putTraceMdc() {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer == null || tracer.currentSpan() == null) {
            return;
        }
        Span span = tracer.currentSpan();
        TraceContext context = span.context();
        putIfPresent("traceId", context.traceId());
        putIfPresent("spanId", context.spanId());
    }

    private void enrichUserMdc(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Jwt jwt = jwtAuthentication.getToken();
            putIfPresent("userId", firstNonBlank(jwt.getSubject(), jwt.getClaimAsString("user_id")));
            putIfPresent("username", firstNonBlank(jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("email")));
            putIfPresent("role", resolveRole(jwtAuthentication.getAuthorities()));
            putIfPresent("actorType", "USER");
            putIfPresent("tenantId", jwt.getClaimAsString("tenant_id"));
            putIfPresent("houseId", jwt.getClaimAsString("house_id"));
            return;
        }
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            putIfPresent("username", principal.getName());
            putIfPresent("actorType", "USER");
        }
    }

    private String resolveRole(Collection<? extends GrantedAuthority> authorities) {
        return authorities == null ? null : authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (properties.getHttp().isTrustCloudflareHeaders()) {
            String cfConnectingIp = request.getHeader("CF-Connecting-IP");
            if (StringUtils.hasText(cfConnectingIp)) {
                return cfConnectingIp.trim();
            }
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void putIfPresent(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        }
    }

    private String firstNonBlank(String... values) {
        return Optional.ofNullable(values)
                .stream()
                .flatMap(java.util.Arrays::stream)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }
}
