package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);
    private final ImportStatusService importStatusService;
    private final CsvProcessingService csvProcessingService;
    private final PersonManagementProperties properties;
    private final StrategyManager strategyManager;

    private final Executor fileProcessingExecutor;
    private final ExecutorService taskExecutor;



//    @Async
//    public void importCsv(MultipartFile file, ImportStatus importStatus) {
//
//        try (Stream<String> lines = new BufferedReader(new InputStreamReader(file.getInputStream())).lines()) {
//            lines.forEach(record -> csvProcessingService.processRecords(record, importStatus));
//        } catch (IOException e) {
//            logger.error("Importing CSV failed with error message: ", e);
//            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, importStatusService.getRowsImportStatus(importStatus.getId()));
//        }
//
//        strategyManager.getAllStrategies().forEach(strategy -> {
//            if (strategy.getBatchSize() != 0) {
//                strategy.processBatch(importStatus);
//            }
//        });
//        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, importStatusService.getRowsImportStatus(importStatus.getId()));
//    }

    @Async("fileProcessingExecutor")
    public void importCsv(MultipartFile file, ImportStatus importStatus) {
        final int CHUNK_SIZE = properties.getBatchSize();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Set<String> processedRecords = ConcurrentHashMap.newKeySet();
            List<String> chunk = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (processedRecords.add(line)) {
                    chunk.add(line);
                    if (chunk.size() == CHUNK_SIZE) {
                        CompletableFuture<Void> future = processChunkAsync(new ArrayList<>(chunk), importStatus);
                        futures.add(future);
                        chunk.clear();
                    }
                }
            }
            if (!chunk.isEmpty()) {
                CompletableFuture<Void> future = processChunkAsync(chunk, importStatus);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (IOException e) {
            logger.error("Error reading the file: ", e);
        }

        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, importStatusService.getRowsImportStatus(importStatus.getId()));
    }

    private CompletableFuture<Void> processChunkAsync(List<String> chunk, ImportStatus importStatus) {
        return CompletableFuture.runAsync(() -> {
            for (String record : chunk) {
                csvProcessingService.processRecords(record, importStatus);
            }
        }, fileProcessingExecutor);
    }

}
