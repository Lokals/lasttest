package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);
    private final ImportStatusService importStatusService;
    private final CsvProcessingService csvProcessingService;
    private final PersonManagementProperties properties;


    @Async("fileProcessingExecutor")
    public void importCsv(MultipartFile file, ImportStatus importStatus) {
        AtomicLong processedRows = new AtomicLong(0);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(file.getInputStream())).lines().parallel()) {
            lines.forEach(record -> executor.submit(new LineProcessingTask(record, csvProcessingService, importStatus, properties.getBatchSize(), processedRows)));
        } catch (IOException e) {
            logger.error("Importing CSV failed with error message: ", e);
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, processedRows.get());
            return;
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }

        List<String> remainingRecords = LineProcessingTask.getRemainingRecords();
        if (!remainingRecords.isEmpty()) {
            csvProcessingService.processBatch(remainingRecords, importStatus);
            processedRows.addAndGet(remainingRecords.size());
        }

        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, processedRows.get());
    }
}
