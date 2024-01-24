package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.factory.ImportStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class LineProcessingTask implements Runnable {

    private static final ThreadLocal<List<String>> threadLocalBatch = ThreadLocal.withInitial(ArrayList::new);
    private final String record;
    private final CsvProcessingService csvProcessingService;
    private final ImportStatus importStatus;
    private final int batchSize;
    private final AtomicLong processedRows;
    private static final Logger logger = LoggerFactory.getLogger(LineProcessingTask.class);



    @Override
    public void run() {
        List<String> batch = threadLocalBatch.get();
        batch.add(record);
        if (batch.size() >= batchSize) {
            try {
                logger.debug("Processing batch of size {}", batch.size());
                csvProcessingService.processBatch(new ArrayList<>(batch), importStatus);
                logger.debug("Processed {} rows so far", processedRows.addAndGet(batch.size()));
            } catch (Exception e) {
                logger.error("Error processing batch", e);
            }
            batch.clear();
        }
    }

    public static List<String> getRemainingRecords() {
        return threadLocalBatch.get();
    }
}
