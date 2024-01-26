package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.dto.EmployeeDto;
import com.mastertest.lasttest.repository.EmployeeRepository;
import com.mastertest.lasttest.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonEmployeeService {

    private final PersonRepository personRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndEmployee(List<EmployeeDto> dtos) {
        List<Employee> employee = dtos.stream()
                .map(this::convertToEmployeeEntity)
                .collect(Collectors.toList());

        personRepository.saveAll(employee);

    }

    private Employee convertToEmployeeEntity(EmployeeDto dto) {
        Employee employee = new Employee();
        employee.setPesel(dto.getPesel());
        employee.setType("employee");
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setHeight(dto.getHeight());
        employee.setWeight(dto.getWeight());
        employee.setEmail(dto.getEmail());
        employee.setPosition(dto.getPosition());
        employee.setSalary(dto.getSalary());
        employee.setEmploymentDate(dto.getEmploymentDate());
        return employee;
    }
}
