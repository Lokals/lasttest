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
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);
    private final ImportStatusService importStatusService;
    private final CsvProcessingService csvProcessingService;
    private final PersonManagementProperties properties;

    @Async
    public void importCsv(MultipartFile file, ImportStatus importStatus) {
        AtomicLong processedRows = new AtomicLong(0);
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(file.getInputStream())).lines()) {
            logger.info(MessageFormat.format("CSV file {0} processing started", file.getOriginalFilename()));
            lines.peek(line -> {
                if (processedRows.incrementAndGet() % properties.getBatchSize() == 0) {
                    logger.debug("Processing batch at row number: {}", processedRows.get());
                    importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, processedRows.get());
                }
            }).forEach(record -> {
                String[] fields = record.split(",");
                if (fields.length > 0){
                    String type = fields[0];
                    csvProcessingService.processRecords(record,type, importStatus);
            }
            });

            logger.info("Completed processing file: {}, total rows processed: {}", importStatus.getFilename(), processedRows.get());
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, processedRows.get());

        } catch (Exception e) {
            logger.error("Improting CSV failed", e);
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, processedRows.get());
        }
    }
}
