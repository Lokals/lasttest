package com.mastertest.lasttest.service.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.PersonDto;

import java.text.ParseException;
import java.util.Map;

public interface PersonService {

    Person getPersonById(Long id);
    PersonDto updatePerson(Long id, Map<String, Object> commandMap) throws ParseException, JsonProcessingException;





}
