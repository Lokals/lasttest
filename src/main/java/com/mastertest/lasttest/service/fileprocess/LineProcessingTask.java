package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.factory.ImportStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class LineProcessingTask implements Runnable {

    private static final ConcurrentLinkedQueue<String> remainingRecordsQueue = new ConcurrentLinkedQueue<>();
    private final String record;
    private final CsvProcessingService csvProcessingService;
    private final ImportStatus importStatus;
    private final int batchSize;
    private final AtomicLong processedRows;
    private static final Logger logger = LoggerFactory.getLogger(LineProcessingTask.class);

    @Override
    public void run() {
        List<String> batch = new ArrayList<>();
        batch.add(record);

        if (batch.size() >= batchSize) {
            processBatch(batch);
        } else {

            remainingRecordsQueue.addAll(batch);

        }
    }

    private void processBatch(List<String> batch) {
        try {
            logger.debug("Processing batch of size {}", batch.size());
            csvProcessingService.processBatch(batch, importStatus);
            logger.debug("Processed {} rows so far", processedRows.addAndGet(batch.size()));
        } catch (Exception e) {
            logger.error("Error processing batch", e);
        }
    }

    public static List<String> getRemainingRecords() {
        List<String> remainingRecords = new ArrayList<>(remainingRecordsQueue);
        remainingRecordsQueue.clear();
        return remainingRecords;

    }
}
