package com.mastertest.lasttest.controller;

import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.model.importfile.StatusFile;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CsvImportService csvImportService;
    @Autowired
    private ImportStatusService importStatusService;
    @Autowired
    private ImportStatusRepository importStatusRepository;

    @AfterEach
    void tearDown() {
        importStatusRepository.deleteAll();
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testGetImportStatus_ReturnsStatus() throws Exception {
        importStatusRepository.deleteAll();
        ImportStatus testStatus = createAndPersistTestImportStatus();

        mockMvc.perform(get("/api/import/" +  testStatus.getId() +"/importstatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testStatus.getId()))
                .andExpect(jsonPath("$.status").value(testStatus.getStatus().toString()))
                .andExpect(jsonPath("$.filename").value(testStatus.getFilename()));

    }

    private ImportStatus createAndPersistTestImportStatus() {
        ImportStatus status = new ImportStatus();
        status.setFilename("testFile.csv");
        status.setStatus(StatusFile.PENDING);
        status.setStartTime(LocalDateTime.now());
        return importStatusRepository.save(status);
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUploadFile_WhenFileIsValid_ReturnsStatusId() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "retiree,FirstName_1306,LastName_73764,71356685741,1.88,71.9,firstname_1306.lastname_73764@example.com,4000.0,15" .getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/import/employees")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andReturn();


        Optional<ImportStatus> importStatus = importStatusRepository.findById(1L);

        assertTrue(importStatus.isPresent());
        assertEquals(StatusFile.COMPLETED, importStatus.get().getStatus());
        assertEquals("test.csv", importStatus.get().getFilename());
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testGetImportStatus_WhenStatusExists_ReturnsStatus() throws Exception {
        ImportStatus importStatus = new ImportStatus();
        importStatus.setFilename("test.csv");
        importStatus.setStatus(StatusFile.COMPLETED);
        importStatus = importStatusRepository.save(importStatus);

        mockMvc.perform(get("/api/import/" + importStatus.getId() + "/importstatus" ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("test.csv"))
                .andExpect(jsonPath("$.status").value(StatusFile.COMPLETED.toString()));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAsyncFileUpload_UpdatesImportStatus() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "asyncTest.csv",
                "text/csv",
                "valid,csv,content" .getBytes()
        );

        mockMvc.perform(multipart("/api/import/employees").file(mockFile))
                .andExpect(status().isOk());

        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<ImportStatus> statuses = importStatusRepository.findAll();
            assertFalse(statuses.isEmpty(), "No import statuses found");

            ImportStatus latestStatus = statuses.get(statuses.size() - 1);

            assertTrue(latestStatus.getStatus() == StatusFile.COMPLETED || latestStatus.getStatus() == StatusFile.FAILED,
                    "Import status was not updated by async process");
        });
    }


}