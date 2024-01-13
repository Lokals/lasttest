package com.mastertest.lasttest.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


public class ConversionUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convertMapToDto(Map<String, Object> map, Class<T> dtoClass) {
        return objectMapper.convertValue(map, dtoClass);
    }

}
