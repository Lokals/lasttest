package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.persons.Person;

public interface PersonService {

    Person getPersonById(String pesel);

    String getPersonType(String pesel);





}
