package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.persons.Employee;
import com.mastertest.lasttest.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonEmployeeService {

    private final PersonRepository personRepository;

    public void savePersonsAndEmployee(Set<Employee> dtos) {
        personRepository.saveAll(dtos);
    }

}
