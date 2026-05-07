package com.isums.common.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

@Converter(autoApply = false)
public class TranslationMapConverter implements AttributeConverter<TranslationMap, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String, String>> STRING_MAP =
            new TypeReference<>() {
            };

    @Override
    public String convertToDatabaseColumn(TranslationMap attribute) {
        if (attribute == null || attribute.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(attribute.asMap());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot serialize TranslationMap", ex);
        }
    }

    @Override
    public TranslationMap convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return TranslationMap.empty();
        try {
            JsonNode node = MAPPER.readTree(dbData);
            JsonNode values = node.has("values") ? node.get("values") : node;
            Map<String, String> map = MAPPER.convertValue(values, STRING_MAP);
            return TranslationMap.of(map);
        } catch (Exception ex) {
            return TranslationMap.empty();
        }
    }
}
