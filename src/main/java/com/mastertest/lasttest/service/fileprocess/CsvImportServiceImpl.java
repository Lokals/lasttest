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
import java.util.stream.Collectors;

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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String> chunk = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                chunk.add(line);
                if (chunk.size() == CHUNK_SIZE) {
                    processChunkInstantly(chunk, importStatus);
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) {
                processChunkInstantly(chunk, importStatus);
            }
        } catch (IOException e) {
            logger.error("Error reading the file: ", e);
        }
        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, importStatusService.getRowsImportStatus(importStatus.getId()));
    }

    private void processChunkInstantly(List<String> chunk, ImportStatus importStatus) {
        List<CompletableFuture<Void>> futures = chunk.stream()
                .map(record -> CompletableFuture.runAsync(() -> csvProcessingService.processRecords(record, importStatus), fileProcessingExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }


}
