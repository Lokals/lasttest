package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.common.FileProcessingException;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Service
public class CsvImportServiceImpl implements CsvImportService {

    private final CsvProcessingService csvProcessingService;
    private final ImportLockService importLockService;
    private final ImportStatusService importStatusService;
    private final FileProcessingAsyncService fileProcessingAsyncService;

    public ImportStatus importCsv(MultipartFile file) {
        boolean locked = importLockService.tryLockImportProcess();
        if (!locked) {
            throw new FileProcessingException("Import is already in progress.");
        }
        ImportStatus importStatus = importStatusService.createNewImportStatus(file.getOriginalFilename());
        try {
            fileProcessingAsyncService.runProcessingFile(file, importStatus, () -> {
                importLockService.unlockImportProcess();
            });
        } catch (Exception e) {
            importLockService.unlockImportProcess();
        }
        return importStatus;
    }
}
