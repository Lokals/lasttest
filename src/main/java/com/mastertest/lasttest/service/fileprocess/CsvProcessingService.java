package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Map;

@AllArgsConstructor
@Service
public class CsvProcessingService {
    private final ImportStatusService importStatusService;
    private Map<String, ImportStrategy<?>> strategies;
    private final StrategyManager strategyManager;

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);


    @Transactional
    public void processRecords(String record,  String type, ImportStatus importStatus) {
        try {
            ImportStrategy<?> strategy = strategyManager.getStrategy(type);
            if (strategy == null) {
                throw new IllegalArgumentException(MessageFormat.format("No strategy found for type: {}",type));
            }
            strategy.validateParseAndSave(record);
        } catch (Exception e) {
            logger.error("Error processing row number {}: {}", importStatus.getProcessedRows(), e.getMessage());
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, importStatus.getProcessedRows());
        }
    }
}
