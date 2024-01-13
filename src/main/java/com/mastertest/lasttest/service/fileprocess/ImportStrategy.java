package com.mastertest.lasttest.service.fileprocess;

import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;

import java.text.ParseException;

public interface ImportStrategy<T extends PersonDto> {

    void validateParseAndSave(String record) throws ParseException;
    PersonDto validateAndSave(CreatePersonCommand<?> personDto);
}
