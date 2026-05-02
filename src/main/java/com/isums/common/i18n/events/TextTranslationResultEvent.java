package com.isums.common.i18n.events;

import java.time.Instant;
import java.util.UUID;

public record TextTranslationResultEvent(
        UUID requestId,
        String resourceType,
        UUID resourceId,
        String fieldName,
        String sourceLanguage,
        String targetLanguage,
        String translatedText,
        String status,
        String errorMessage,
        Instant translatedAt
) {
    public static final String STATUS_DONE = "DONE";
}
