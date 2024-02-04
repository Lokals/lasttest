package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchProcessService {

    private final StrategyManager strategyManager;


    public void processBatchAsync(ImportStatus importStatus) {
        strategyManager.getAllStrategies().forEach(strategy -> {
            strategy.processBatch();
        });
    }
}