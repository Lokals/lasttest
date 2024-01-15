package com.mastertest.lasttest.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;


public class ConversionUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convertMapToDto(Map<String, Object> map, Class<T> dtoClass) {
        return objectMapper.convertValue(map, dtoClass);
    }

    public static <T> T convertMapToCommand(Map<String, Object> map, Class<T> commandClass) {
        return objectMapper.convertValue(map, commandClass);
    }

}
