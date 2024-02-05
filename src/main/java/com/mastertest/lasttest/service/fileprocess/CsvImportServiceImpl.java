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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArraySet;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);
    private final ImportStatusService importStatusService;
    private final CsvProcessingService csvProcessingService;
    private final PersonManagementProperties properties;


    @Async("fileProcessingExecutor")
    public void importCsv(MultipartFile file, ImportStatus importStatus) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            CopyOnWriteArraySet<String> batch = new CopyOnWriteArraySet<>();
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, 0L);
            while ((line = fileReader.readLine()) != null) {
                batch.add(line);
                if (batch.size() >= properties.getBatchSize()) {
                    CopyOnWriteArraySet<String> batchToProcess = new CopyOnWriteArraySet<>(batch);
                    csvProcessingService.processBatchAsync(batchToProcess, importStatus);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                csvProcessingService.processBatchAsync(batch, importStatus);
            }
        } catch (Exception e) {
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, importStatusService.getRowsImportStatus(importStatus.getId()));
            logger.error("Error reading the file: ", e);
        }
        finally {
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, importStatusService.getRowsImportStatus(importStatus.getId()));
        }
    }


}
