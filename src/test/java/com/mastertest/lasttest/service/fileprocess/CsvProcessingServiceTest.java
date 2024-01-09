package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.configuratio.PersonManagementProperties;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvProcessingServiceTest {

    @InjectMocks
    private CsvProcessingService csvProcessingService;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ImportStatusService importStatusService;

    @Mock
    private PersonManagementProperties managementProperties;

    private static CSVRecord record;
    private static ImportStatus importStatus;

    @BeforeEach
    void setUp() throws ParseException {
        record = mock(CSVRecord.class);
        importStatus = new ImportStatus();
        importStatus.setId(1L);

        when(managementProperties.getBatchSize()).thenReturn(10);

        when(record.get("type")).thenReturn("student");
        when(record.get("firstName")).thenReturn("Test");
        when(record.get("lastName")).thenReturn("Test");
        when(record.get("height")).thenReturn("180");
        when(record.get("weight")).thenReturn("80");
        when(record.get("email")).thenReturn("test.test@example.com");
        when(record.get("pesel")).thenReturn("12345678901");
        when(record.get("universityName")).thenReturn("University");
        when(record.get("yearOfStudy")).thenReturn("2");
        when(record.get("scholarship")).thenReturn("1000");
        when(record.get("studyField")).thenReturn("Computer");
    }

    @Test
    void processRecords_WithValidRecords_UpdatesStatusAndProcessesRecords() throws ParseException {
        List<CSVRecord> records = new ArrayList<>();
        records.add(record);

        csvProcessingService.processRecords(records, importStatus);

        verify(personRepository, times(1)).saveAll(anyList());
        verify(importStatusService, atLeastOnce()).updateImportStatus(eq(1L), any(StatusFile.class), anyLong());
    }


    @Test
    void processRecords_WithExceptionOnSave_UpdatesStatusToFailed() {
        List<CSVRecord> records = new ArrayList<>();
        records.add(record);

        doThrow(new DataIntegrityViolationException("Database error")).when(personRepository).saveAll(anyList());

        assertThrows(DataIntegrityViolationException.class, () -> csvProcessingService.processRecords(records, importStatus));

        verify(importStatusService).updateImportStatus(eq(1L), eq(StatusFile.INPROGRESS), anyLong());
    }

}