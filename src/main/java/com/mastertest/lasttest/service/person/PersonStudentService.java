package com.mastertest.lasttest.service.person;


import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonStudentService {

    private final PersonRepository personRepository;
    private final StudentRepository studentRepository;
    private static final Logger logger = LoggerFactory.getLogger(PersonStudentService.class);

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndStudents(List<Student> students) {
        logger.debug("STUDENT LIST SIZE: {}", students.size());

        personRepository.saveAll(students);

    }
}
