package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.repository.EmployeeRepository;
import com.mastertest.lasttest.repository.PersonRepository;
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
public class PersonEmployeeService {

    private final PersonRepository personRepository;
    private final EmployeeRepository employeeRepository;
    private static final Logger logger = LoggerFactory.getLogger(PersonEmployeeService.class);

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndEmployee(List<Employee> dtos) {
        logger.debug("EMPLOYEE LIST SIZE: {}", dtos.size());
        personRepository.saveAll(dtos);

    }
}
