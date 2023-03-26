package com.hackeruso.automation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class JsonConverter {

    private static final ObjectMapper objectMapper = getDefaultObjectMapper();

    private static ObjectMapper getDefaultObjectMapper() {
        ObjectMapper defaultObjectMapper = new ObjectMapper();
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return defaultObjectMapper;
    }

    public static JsonNode parse(String jsonString) throws IOException {
        return objectMapper.readTree(jsonString);
    }

    public static <T> T fromJson(JsonNode node, Class<T> tClass) throws JsonProcessingException {
        return objectMapper.treeToValue(node, tClass);
    }

    public static JsonNode toJson(Object obj) {
        return objectMapper.valueToTree(obj);
    }

    /**
     * return node & values in one single line
     */
    public static String stringify(JsonNode node) throws JsonProcessingException {
        return generateString(node,false);
    }

    /**
     * same as above return pretty print (not 1 line)
     */
    public static String prettyPrint(JsonNode node) throws JsonProcessingException {
        return generateString(node,true);
    }


    private static String generateString(JsonNode node, boolean pretty) throws JsonProcessingException {
        ObjectWriter objectWriter = objectMapper.writer();
        if (pretty) {
            objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);
        }
        return objectWriter.writeValueAsString(node);
    }

    public static <T> T getJsonData(String jsonFilePath, Class<T> POJO) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(FileUtil.getFile(jsonFilePath),POJO);
    }
}
