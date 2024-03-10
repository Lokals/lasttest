package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.model.importfile.StatusFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceImplTest {

    @InjectMocks
    private CsvImportServiceImpl csvImportService;

    @Mock
    private ImportStatusService importStatusService;

    @Mock
    private CsvProcessingService csvProcessingService;

    @Mock
    private MultipartFile mockFile;
    @Mock
    private PersonManagementProperties managementProperties;

    private static ImportStatus importStatus;
    private final String record = "student,Jan,Kowalski,12345678901,1.80,75,jan.kowalski@example.com,Uniwersytet Warszawski,3,Informatyka,1000.0";

    @BeforeEach
    void setUp() {
        importStatus = new ImportStatus();
        importStatus.setId(1L);
        importStatus.setProcessedRows(0L);
        Mockito.lenient().when(managementProperties.getBatchSize()).thenReturn(10);

    }

    @Test
    void testImportCsv_ProcessesFileCorrectly() throws Exception {
        String fileContent = "student,Jan,Kowalski,12345678901,1.80,75,jan.kowalski@example.com,Uniwersytet Warszawski,3,Informatyka,1000.0";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        csvImportService.importCsv(mockFile);

//        verify(csvProcessingService, times(1)).processRecords(anyString(), eq(importStatus));
        verify(importStatusService, atLeastOnce()).updateImportStatus(eq(importStatus.getId()), any(StatusFile.class), anyLong());
    }


    @Test
    void testImportCsv_WhenErrorOccurs_ShouldUpdateStatusToFailed() throws Exception {
        when(mockFile.getInputStream()).thenThrow(new IOException("Read error"));

        csvImportService.importCsv(mockFile);

        verify(importStatusService).updateImportStatus(eq(importStatus.getId()), eq(StatusFile.FAILED), eq(0L));
    }


}