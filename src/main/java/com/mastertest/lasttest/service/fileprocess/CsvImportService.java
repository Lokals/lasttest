package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.factory.ImportStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CsvImportService {

    void importCsv(MultipartFile file, ImportStatus importStatus) throws IOException;

}
