package com.mastertest.lasttest.strategy.update;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateStrategyManager {

    private final Map<String, UpdateStrategy> strategyMap;
    private final ApplicationContext context;


    public UpdateStrategyManager(ApplicationContext context) {
        this.context = context;
        strategyMap = this.context.getBeansOfType(UpdateStrategy.class)
                .values()
                .stream()
                .collect(Collectors.toMap(
                        strategy -> extractTypeFromStrategyName(strategy.getClass().getSimpleName()),
                        strategy -> strategy
                ));
    }

    public UpdateStrategy getUpdateStrategy(String type) {
        return strategyMap.getOrDefault(type.toLowerCase(), null);
    }

    private String extractTypeFromStrategyName(String strategyName) {
        return strategyName.replaceAll("UpdateStrategy$", "").toLowerCase();
    }
    public <T extends UpdatePersonCommand> T convertCommand(UpdatePersonCommand command, Class<T> specificClass) {
        return context.getBean(ObjectMapper.class).convertValue(command, specificClass);
    }
}
