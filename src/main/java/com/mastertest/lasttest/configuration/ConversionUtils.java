package com.mastertest.lasttest.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class ConversionUtils {

    private final ObjectMapper objectMapper;

    public <T> T convertMapToDto(Map<String, Object> map, Class<T> dtoClass) {
        return objectMapper.convertValue(map, dtoClass);
    }

    public <T> T convertCommandToCommand(Object command, Class<T> commandClass) {
        return objectMapper.convertValue(command, commandClass);
    }
}
