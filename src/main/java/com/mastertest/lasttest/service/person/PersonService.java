package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;

import java.text.ParseException;

public interface PersonService {

    PersonDto save(CreatePersonCommand command) throws ParseException;

    PersonDto findById(Long id);
    Person getPersonById(Long id);


    PersonDto update(Long id, UpdatePersonCommand command);


}
