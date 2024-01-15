package com.mastertest.lasttest.controller;


import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
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
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (!importStatusRepository.findByStatuses(List.of(StatusFile.INPROGRESS, StatusFile.PENDING)).isEmpty()) {
            return ResponseEntity.badRequest().body("Already of some import is during processing");
        }
        ImportStatus importStatus = importStatusService.createNewImportStatus(file.getOriginalFilename());
        csvImportService.importCsv(file, importStatus);
        return ResponseEntity.ok(importStatus.getId());
    }

    @GetMapping("/importstatus/{id}")
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable Long id) {
        ImportStatus status = importStatusService.getImportStatus(id);
        return ResponseEntity.ok(status);
    }
}
