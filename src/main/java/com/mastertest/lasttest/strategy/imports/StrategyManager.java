package com.mastertest.lasttest.strategy.imports;


import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class StrategyManager {

    private static final Logger logger = LoggerFactory.getLogger(StrategyManager.class);

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
        return strategyName.replaceAll("ImportStrategy.*$", "").toLowerCase();
    }

    public ImportStrategy<?> getStrategy(String type) {
        logger.debug("Loaded strategies: {}", strategyMap.keySet());
        ImportStrategy<?> strategy = strategyMap.get(type.toLowerCase());
        if (strategy == null) {
            logger.warn("No strategy found for type: {}", type);
        } else {
            logger.debug("Strategy found for type: {}", type);
        }
        return strategy;
    }

    public Collection<ImportStrategy<?>> getAllStrategies() {
        return strategyMap.values();
    }
}
