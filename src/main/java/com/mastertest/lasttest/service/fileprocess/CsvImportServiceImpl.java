package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);
    private final ImportStatusService importStatusService;
    private final CsvProcessingService csvProcessingService;

    @Async
    public void importCsv(MultipartFile file, ImportStatus importStatus) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            logger.info(MessageFormat.format("CSV file {0} processing started", file.getOriginalFilename()));
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            csvProcessingService.processRecords(records, importStatus);
        } catch (Exception e) {
            logger.error("Improting CSV failed", e);
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, 0L);
        }
    }
}
