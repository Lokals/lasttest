package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CsvProcessingService {

    private final StrategyManager strategyManager;
    private final PersonManagementProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);


    @Transactional
    public void processRecords(String record, ImportStatus importStatus) {


        try {
            String[] fields = record.split(",");
            if (fields.length > 0) {
                String type = fields[0];
                logger.debug("Attempting to process record of type: {}", type);

                ImportStrategy<?> strategy = strategyManager.getStrategy(type);
                if (strategy == null) {
                    logger.error("No strategy found for type: {}", type);
                    return;
                }
                strategy.addToBatch(record);
            } else {
                logger.error("Record does not have a type: {}", record);
            }
        } catch (Exception e) {
            logger.error("Error processing row: {}", record, e);
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
