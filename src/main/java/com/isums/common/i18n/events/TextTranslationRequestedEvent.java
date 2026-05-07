package com.isums.common.i18n.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TextTranslationRequestedEvent(
        UUID requestId,
        String resourceType,
        UUID resourceId,
        String fieldName,
        String sourceText,
        String sourceLanguage,
        List<String> targetLanguages,
        TranslationIntent intent,
        Boolean preserveFormatting,
        Instant requestedAt,
        String callbackTopic
) {
    public static final String TOPIC = "text.translation.requested";
}
