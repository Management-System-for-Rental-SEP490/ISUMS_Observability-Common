package com.isums.common.i18n;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SupportedLocales {
    public static final Set<String> ALL = Set.of("vi", "en", "ja");

    private SupportedLocales() {
    }

    public static boolean isSupported(String locale) {
        String normalized = TranslationMap.normalizeLanguage(locale);
        return normalized != null && ALL.contains(normalized);
    }

    public static Set<String> normalizeAll(Iterable<String> locales) {
        Set<String> out = new LinkedHashSet<>();
        if (locales == null) return out;
        for (String locale : locales) {
            String normalized = TranslationMap.normalizeLanguage(locale);
            if (normalized != null && isSupported(normalized)) out.add(normalized);
        }
        return out;
    }
}
