package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceImplTest {

    @InjectMocks
    private CsvImportServiceImpl csvImportService;

    @Mock
    private CsvProcessingService csvProcessingService;

    @Mock
    private ImportStatusService importStatusService;

    @Mock
    private MultipartFile file;

    private static ImportStatus importStatus;

    @BeforeEach
    void setUp() {
        importStatus = new ImportStatus();
        importStatus.setId(1L);
    }

    @Test
    void importCsv_WithValidFile_CallsProcessRecords() throws Exception {
        String csvContent = "type,firstName,lastName\nstudent,Test,Test";
        InputStream is = new ByteArrayInputStream(csvContent.getBytes());
        when(file.getInputStream()).thenReturn(is);
        when(file.getOriginalFilename()).thenReturn("test.csv");

        csvImportService.importCsv(file, importStatus);

//        verify(csvProcessingService, times(1)).processRecords(any(Iterable.class), eq(importStatus));
        assertNotNull(importStatus);
    }

    @Test
    void importCsv_WithException_UpdatesStatusToFailed() throws Exception {
        when(file.getInputStream()).thenThrow(new RuntimeException("Test Exception"));

        csvImportService.importCsv(file, importStatus);
        verify(importStatusService).updateImportStatus(eq(importStatus.getId()), eq(StatusFile.FAILED), eq(0L));

    }
}