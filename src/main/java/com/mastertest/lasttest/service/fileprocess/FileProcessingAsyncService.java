package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.model.importfile.ImportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class FileProcessingAsyncService {

    private final CsvProcessingService csvProcessingService;
    @Async("fileProcessingExecutor")
    public CompletableFuture<Void> runProcessingFile(MultipartFile file, ImportStatus status, Runnable onFinish){
        try {
            csvProcessingService.processFile(file, status);
        } finally {
            onFinish.run();
        }
        return CompletableFuture.completedFuture(null);
    }

}
