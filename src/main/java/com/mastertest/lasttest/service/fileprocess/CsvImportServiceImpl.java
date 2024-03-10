package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.common.FileProcessingException;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private final CsvProcessingService csvProcessingService;
    private final ImportLockService importLockService;
    private final ImportStatusService importStatusService;

    public ImportStatus importCsv(MultipartFile file) {
        if (!importLockService.tryLockImportProcess()) {
            throw new FileProcessingException("Import is already in progress.");
        }
        try {
            ImportStatus importStatus = importStatusService.createNewImportStatus(file.getOriginalFilename());
            runProcessingFile(file, importStatus);
            return importStatus;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            importLockService.unlockImportProcess();
        }
    }

    @Async("fileProcessingExecutor")
    private void runProcessingFile(MultipartFile file, ImportStatus status){
        csvProcessingService.processFile(file, status);
    }


}
