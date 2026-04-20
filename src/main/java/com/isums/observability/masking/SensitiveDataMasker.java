package com.isums.observability.masking;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "pass",
            "pwd",
            "token",
            "access_token",
            "refresh_token",
            "authorization",
            "jwt",
            "otp",
            "secret",
            "client_secret",
            "private_key",
            "api_key");
    private static final Pattern BEARER_TOKEN = Pattern.compile("Bearer\\s+[A-Za-z0-9._~+\\-/]+=*", Pattern.CASE_INSENSITIVE);
    private static final Pattern JWT = Pattern.compile("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");

    private SensitiveDataMasker() {
    }

    public static Object mask(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                sanitized.put(key, isSensitiveKey(key) ? MASK : mask(entry.getValue()));
            }
            return sanitized;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(SensitiveDataMasker::mask).toList();
        }
        if (value instanceof String string) {
            return maskString(string);
        }
        return value;
    }

    public static Map<String, Object> maskMap(Map<String, Object> metadata) {
        Object masked = mask(metadata);
        if (masked instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
            return result;
        }
        return Map.of();
    }

    public static String maskString(String value) {
        if (value == null) {
            return null;
        }
        String masked = BEARER_TOKEN.matcher(value).replaceAll("Bearer " + MASK);
        return JWT.matcher(masked).replaceAll(MASK);
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key.toLowerCase().replace("-", "_");
        return SENSITIVE_KEYS.stream().anyMatch(normalized::contains);
    }
}
