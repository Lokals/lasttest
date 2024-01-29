package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CsvProcessingService {

    private final StrategyManager strategyManager;
    private final PersonManagementProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
    @Transactional
    public void processRecords(String record, ImportStatus importStatus) {

        try {
            String[] fields = record.split(",");
            if (fields.length > 0) {
                String type = fields[0];
                ImportStrategy<?> strategy = strategyManager.getStrategy(type);
                strategy.addToBatch(record);
            } else {
                throw new IllegalArgumentException("No strategy found  type");
            }
        } catch (Exception e) {
            logger.error("Error processing row number {}: {}", importStatus.getProcessedRows(), e.getMessage());
        }
        strategyManager.getAllStrategies().forEach(strategy -> {
            if (strategy.getBatchSize() >= properties.getBatchSize()) {
                strategy.processBatch(importStatus);
            }
        });
    }


    @Transactional
    public void processBatch(List<String> batch, ImportStatus importStatus) {
        batch.forEach(record -> {
            try {
                String[] fields = record.split(",");
                if (fields.length > 0) {
                    String type = fields[0];
                    ImportStrategy<?> strategy = strategyManager.getStrategy(type);
                    strategy.addToBatch(record);
                }
            } catch (Exception e) {
                logger.error("Error processing row number {}: {}", importStatus.getProcessedRows(), e);
            }
        });
        logger.info("Processing bath!!");

        strategyManager.getAllStrategies().forEach(strategy -> strategy.processBatch(importStatus));

    }
}
