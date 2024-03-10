package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.importfile.ImportStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CsvImportService {

    ImportStatus importCsv(MultipartFile file) throws IOException;

}
