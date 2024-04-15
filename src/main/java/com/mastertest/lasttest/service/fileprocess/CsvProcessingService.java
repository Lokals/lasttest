package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.model.importfile.StatusFile;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@Service
public class CsvProcessingService {

    private final StrategyManager strategyManager;
    private final ImportStatusService importStatusService;
    private final PersonManagementProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);


    private void processRecords(String record, ImportStatus importStatus) {
        try {
            String[] fields = record.split(",");
            if (fields.length > 0) {
                String type = fields[0].toLowerCase();
                ImportStrategy<?> strategy = strategyManager.getStrategy(type);
                if (strategy != null) {
                    strategy.addToBatch(record, importStatus);
                } else {
                    throw new IllegalArgumentException("No strategy found for type: " + type);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing row: " + record, e);
        }
    }
    @Transactional
    private void runBatchInserts() {
        strategyManager.getAllStrategies().forEach(ImportStrategy::processBatch);
    }


    public void processFile(MultipartFile file, ImportStatus importStatus) {
        AtomicInteger processedLines = new AtomicInteger(0);
        List<String> batch = new ArrayList<>();
        Instant now = Instant.now();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, 0L);
            fileReader.lines().forEach(line -> {
                batch.add(line);
                processRecords(line, importStatus);
                if (batch.size() >= properties.getBatchSize()) {
                    processedLines.addAndGet(batch.size());
                    runBatchInserts();
                    importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS , processedLines.longValue());
                    logger.info("saving batch - records: {}  - time passed: {}", processedLines, ChronoUnit.SECONDS.between(now, Instant.now()));
                    batch.clear();
                }
            });

            if (!batch.isEmpty()) {
                runBatchInserts();
                importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS , processedLines.longValue());
            }
        } catch (IOException e) {
            logger.error("Error reading the file: ", e);
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, importStatusService.getRowsImportStatus(importStatus.getId()));
            return;
        }
        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, importStatusService.getRowsImportStatus(importStatus.getId()));
    }
}
