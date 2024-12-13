package com.big.screen.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 当遇到未知属性（没有对应的属性来映射）时，是否引发异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }
}