package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.model.importfile.StatusFile;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ImportStatusServiceTest {

    @InjectMocks
    private ImportStatusService importStatusService;

    @Mock
    private ImportStatusRepository importStatusRepository;

    private static ImportStatus importStatus;

    @BeforeEach
    void setUp() {
        importStatus = new ImportStatus();
        importStatus.setId(1L);
        importStatus.setFilename("test.csv");
        importStatus.setStatus(StatusFile.PENDING);
        importStatus.setStartTime(LocalDateTime.now());
        importStatus.setProcessedRows(0L);
    }

    @Test
    void createNewImportStatus_SuccessfullyCreatesNewStatus() {
        when(importStatusRepository.save(any(ImportStatus.class))).thenReturn(importStatus);

        ImportStatus createdStatus = importStatusService.createNewImportStatus("test.csv");

        assertNotNull(createdStatus);
        assertEquals("test.csv", createdStatus.getFilename());
        assertEquals(StatusFile.PENDING, createdStatus.getStatus());
        verify(importStatusRepository).save(any(ImportStatus.class));
    }

    @Test
    void updateImportStatus_SuccessfulUpdate() {
        when(importStatusRepository.findById(1L)).thenReturn(Optional.of(importStatus));

        importStatusService.updateImportStatus(1L, StatusFile.INPROGRESS, 100L);

        assertEquals(StatusFile.INPROGRESS, importStatus.getStatus());
        assertEquals(100L, importStatus.getProcessedRows());
        verify(importStatusRepository).findById(1L);
        verify(importStatusRepository).save(importStatus);
    }

    @Test
    void updateImportStatus_StatusNotFound_ThrowsEntityNotFoundException() {
        when(importStatusRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                importStatusService.updateImportStatus(1L, StatusFile.INPROGRESS, 100L));

        verify(importStatusRepository).findById(1L);
        verify(importStatusRepository, never()).save(any(ImportStatus.class));
    }

    @Test
    void getImportStatus_SuccessfulRetrieval() {
        when(importStatusRepository.findById(1L)).thenReturn(Optional.of(importStatus));

        ImportStatus retrievedStatus = importStatusService.getImportStatus(1L);

        assertNotNull(retrievedStatus);
        assertEquals(1L, retrievedStatus.getId());
        verify(importStatusRepository).findById(1L);
    }


    @Test
    void updateImportStatus_RepositoryThrowsRuntimeException_ThrowsRuntimeException() {
        when(importStatusRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () ->
                importStatusService.updateImportStatus(1L, StatusFile.INPROGRESS, 100L));

        verify(importStatusRepository).findById(1L);
        verify(importStatusRepository, never()).save(any(ImportStatus.class));
    }

    @Test
    void getImportStatus_StatusNotFound_ThrowsEntityNotFoundException() {
        when(importStatusRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                importStatusService.getImportStatus(1L));

        verify(importStatusRepository).findById(1L);
    }
}