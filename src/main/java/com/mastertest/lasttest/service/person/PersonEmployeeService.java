package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.repository.EmployeeRepository;
import com.mastertest.lasttest.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonEmployeeService {

    private final PersonRepository personRepository;
    private final EmployeeRepository employeeRepository;

//    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
//    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndEmployee(List<Employee> dtos) {
        personRepository.saveAll(dtos);
    }

}
