package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.model.importfile.ImportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileProcessingAsyncService {

    private final CsvProcessingService csvProcessingService;
    @Async("fileProcessingExecutor")
    public void runProcessingFile(MultipartFile file, ImportStatus status){
        csvProcessingService.processFile(file, status);
    }

}
