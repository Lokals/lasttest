package com.mastertest.lasttest.strategy;


import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StrategyManager {
    private final Map<String, ImportStrategy<?>> strategyMap;
    public StrategyManager(ApplicationContext context) {
        this.strategyMap = context.getBeansOfType(ImportStrategy.class)
                .values()
                .stream()
                .collect(Collectors.toMap(
                        strategy -> extractTypeFromStrategyName(strategy.getClass().getSimpleName()),
                        strategy -> strategy
                ));
    }

    private String extractTypeFromStrategyName(String strategyName) {
        return strategyName.replaceAll("ImportStrategy$", "").toLowerCase();
    }

    public ImportStrategy<?> getStrategy(String type) {
        return strategyMap.get(type.toLowerCase());
    }
}
