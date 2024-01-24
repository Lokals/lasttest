package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ImportStatusService {
    private static final Logger logger = LoggerFactory.getLogger(ImportStatusService.class);

    private final ImportStatusRepository importStatusRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ImportStatus createNewImportStatus(String filename) {
        logger.info("Creating new import status for file: {}", filename);
        ImportStatus status = new ImportStatus();
        status.setFilename(filename);
        status.setStatus(StatusFile.PENDING);
        status.setStartTime(LocalDateTime.now());
        status.setProcessedRows(0L);
        ImportStatus savedStatus = importStatusRepository.save(status);
        logger.debug("New import status saved with id: {}", savedStatus.getId());
        return savedStatus;
    }
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateImportStatus(Long id, StatusFile status, Long processedRows) {
        logger.info("Updating import status for id: {} to {}", id, status);
        ImportStatus importStatus = getImportStatus(id);
        importStatus.setStatus(status);
        importStatus.setProcessedRows(processedRows);
        if (status == StatusFile.COMPLETED || status == StatusFile.FAILED) {
            importStatus.setEndTime(LocalDateTime.now());
        }
        importStatusRepository.save(importStatus);
        logger.debug("Import status updated for id: {}", id);

    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Long getRowsImportStatus(Long id) {
        return importStatusRepository.findProcessedRowsById(id);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ImportStatus getImportStatus(Long id) {
        logger.info("Retrieving import status for id: {}", id);

        return importStatusRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("ImportStatus not found with id: {}", id);
                    return new EntityNotFoundException("ImportStatus not found with id " + id);

                });
    }
}
