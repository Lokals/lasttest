package com.mastertest.lasttest.service.person;


import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonStudentService {

    private final PersonRepository personRepository;


    public void savePersonsAndStudents(Set<Student> students) {
        personRepository.saveAll(students);

    }
}
