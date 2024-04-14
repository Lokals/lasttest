package com.mastertest.lasttest.controller;


import com.mastertest.lasttest.common.FileProcessingException;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/import")
@RequiredArgsConstructor
public class ImportFileController {


    private final CsvImportService csvImportService;
    private final ImportStatusService importStatusService;



    @PostMapping("/employees")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            ImportStatus importStatus = csvImportService.importCsv(file);
            return ResponseEntity.ok(importStatusService.getImportStatus(importStatus.getId()));
        } catch (FileProcessingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
        }
    }


    @GetMapping("{id}/importstatus")
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable Long id) {
        ImportStatus status = importStatusService.getImportStatus(id);
        return ResponseEntity.ok(status);
    }
}
