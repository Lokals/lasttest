package com.mastertest.lasttest.controller;


import com.mastertest.lasttest.common.FileProcessingException;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/import")
@RequiredArgsConstructor
public class ImportFileController {


    private final CsvImportService csvImportService;
    private final ImportStatusService importStatusService;
    private final ImportStatusRepository importStatusRepository;


    @PostMapping("/employees")
    @Transactional
    public ResponseEntity<ImportStatus> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        synchronized (this) {
            if (!importStatusRepository.findByStatuses(List.of(StatusFile.INPROGRESS, StatusFile.PENDING)).isEmpty()) {
                throw new FileProcessingException("Already of some import is during processing");
            }
            ImportStatus importStatus = importStatusService.createNewImportStatus(file.getOriginalFilename());

            csvImportService.importCsv(file, importStatus);
            return ResponseEntity.ok(importStatusService.getImportStatus(importStatus.getId()));
        }
    }

    @GetMapping("{id}/importstatus")
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable Long id) {
        ImportStatus status = importStatusService.getImportStatus(id);
        return ResponseEntity.ok(status);
    }
}
