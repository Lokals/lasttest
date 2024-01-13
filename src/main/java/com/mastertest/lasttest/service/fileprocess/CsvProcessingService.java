package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.strategy.StrategyManager;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.mastertest.lasttest.service.fileprocess.CsvRecordValidator.*;
import static com.mastertest.lasttest.service.fileprocess.CsvRecordValidator.isValidYearsWorked;

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
//            ImportStrategy<?> strategy = strategies.get(type.toLowerCase());
//            if (strategy == null) {
//                throw new IllegalArgumentException("No strategy found for type: " + type);
//            }
            strategy.validateParseAndSave(record);
        } catch (Exception e) {
            logger.error("Error processing row number {}: {}", importStatus.getProcessedRows(), e.getMessage());
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, importStatus.getProcessedRows());
        }
    }
}
