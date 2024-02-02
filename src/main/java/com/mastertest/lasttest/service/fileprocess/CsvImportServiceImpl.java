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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);
    private final ImportStatusService importStatusService;
    private final CsvProcessingService csvProcessingService;
    private final PersonManagementProperties properties;
    private final StrategyManager strategyManager;
    private final Executor fileProcessingExecutor;
    private final BatchProcessService batchProcessService;

    @Async("fileProcessingExecutor")
    public void importCsv(MultipartFile file, ImportStatus importStatus) {
        final int CHUNK_SIZE = properties.getBatchSize();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String> chunk = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                chunk.add(line);
                if (chunk.size() == CHUNK_SIZE) {
                    CompletableFuture<Void> future = processChunkAsync(new ArrayList<>(chunk), importStatus);
                    futures.add(future);
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) {
                CompletableFuture<Void> future = processChunkAsync(new ArrayList<>(chunk), importStatus);
                futures.add(future);
            }
        } catch (IOException e) {
            logger.error("Error reading the file: ", e);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        batchProcessService.processBatchAsync(importStatus);


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
