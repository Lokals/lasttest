package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.importfile.ImportStatus;

import java.text.ParseException;

public interface ImportStrategy<T extends PersonDto> {


    PersonDto validateAndSave(CreatePersonCommand<?> personDto);

    void  addToBatch(String record, ImportStatus importStatus) throws ParseException;

    void processBatch();

    void clearBatch();

}
