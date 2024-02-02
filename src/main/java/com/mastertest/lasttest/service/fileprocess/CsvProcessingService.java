package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.strategy.imports.StrategyManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CsvProcessingService {

    private final StrategyManager strategyManager;
    private final PersonManagementProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);

    @Transactional
    public void processRecords(String record, ImportStatus importStatus) {
        try {
            String[] fields = record.split(",");
            if (fields.length > 0) {
                String type = fields[0].toLowerCase();
                ImportStrategy<?> strategy = strategyManager.getStrategy(type);
                if (strategy != null) {
                    strategy.addToBatch(record);
                } else {
                    throw new IllegalArgumentException("No strategy found for type: " + type);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing row: " + record, e);
        }
    }

}
