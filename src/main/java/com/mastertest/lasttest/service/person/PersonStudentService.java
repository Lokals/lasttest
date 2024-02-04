package com.mastertest.lasttest.service.person;


import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonStudentService {

    private final PersonRepository personRepository;
    private final StudentRepository studentRepository;

//    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
//    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndStudents(List<Student> students) {

        personRepository.saveAll(students);

    }
}
