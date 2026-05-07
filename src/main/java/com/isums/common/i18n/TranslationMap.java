package com.isums.common.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TranslationMap implements Serializable {
    private final Map<String, String> values;

    public TranslationMap() {
        this.values = new LinkedHashMap<>();
    }

    @JsonCreator
    public TranslationMap(Map<String, String> values) {
        this.values = normalize(values);
    }

    public static TranslationMap empty() {
        return new TranslationMap();
    }

    public static TranslationMap of(Map<String, String> values) {
        return new TranslationMap(values);
    }

    public static String normalizeLanguage(String raw) {
        if (raw == null) return null;
        String value = raw.trim().toLowerCase().replace('_', '-');
        if (value.isBlank()) return null;
        int dash = value.indexOf('-');
        if (dash > 0) value = value.substring(0, dash);
        return value.isBlank() ? null : value;
    }

    @JsonValue
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(values);
    }

    public Set<String> languagesPresent() {
        return values.keySet();
    }

    public TranslationMap mergeAutoFilled(Map<String, String> patch) {
        Map<String, String> merged = new LinkedHashMap<>(values);
        merged.putAll(normalize(patch));
        return new TranslationMap(merged);
    }

    public String get(String language) {
        String normalized = normalizeLanguage(language);
        return normalized == null ? null : values.get(normalized);
    }

    public String resolve(String preferredLanguage) {
        String preferred = get(preferredLanguage);
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        String vi = get("vi");
        if (vi != null && !vi.isBlank()) {
            return vi;
        }
        for (String value : values.values()) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    private static Map<String, String> normalize(Map<String, String> input) {
        Map<String, String> out = new LinkedHashMap<>();
        if (input == null) return out;
        input.forEach((language, text) -> {
            String normalized = normalizeLanguage(language);
            if (normalized != null && text != null && !text.isBlank()) {
                out.put(normalized, text);
            }
        });
        return out;
    }
}
