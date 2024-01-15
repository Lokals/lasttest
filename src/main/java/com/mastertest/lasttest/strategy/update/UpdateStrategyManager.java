package com.mastertest.lasttest.strategy.update;


import com.mastertest.lasttest.service.person.UpdateStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateStrategyManager {

    private final Map<String, UpdateStrategy> strategyMap;

    public UpdateStrategyManager(ApplicationContext context) {
        strategyMap = context.getBeansOfType(UpdateStrategy.class)
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
}
