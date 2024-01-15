package com.mastertest.lasttest.service.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;

import java.text.ParseException;
import java.util.Map;

public interface UpdateStrategy<T extends UpdatePersonCommand> {
    PersonDto updateAndValidate(Map<String, Object> map, Person person) throws JsonProcessingException, ParseException;

}
