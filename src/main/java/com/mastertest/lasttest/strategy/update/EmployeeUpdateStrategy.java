package com.mastertest.lasttest.strategy.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.EmployeeDto;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeeCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class EmployeeUpdateStrategy implements UpdateStrategy<UpdateEmployeeCommand> {

    private final PersonRepository personRepository;
    private final PersonValidator personValidator;

    @Override
    public PersonDto updateAndValidate(Map<String, Object> updateCommand, Person person) throws JsonProcessingException, ParseException {

        Employee employee = (Employee) person;
        UpdateEmployeeCommand employeeCommand = ConversionUtils.convertMapToCommand(updateCommand, UpdateEmployeeCommand.class);
        updateCommonFields(employeeCommand, employee);

        if (employeeCommand.getEmploymentDate() != null) {
            employee.setEmploymentDate(employeeCommand.getEmploymentDate());
        }
        if (employeeCommand.getSalary() != null) {
            employee.setSalary(employeeCommand.getSalary());
        }
        if (employeeCommand.getPosition() != null) {
            employee.setPosition(employeeCommand.getPosition());
        }

        return convertToPersonDto(employee);

    }

    private void updateCommonFields(UpdatePersonCommand updateCommand, Employee employee) {
        if (updateCommand.getFirstName() != null) {
            employee.setFirstName(updateCommand.getFirstName());
        }
        if (updateCommand.getLastName() != null) {
            employee.setLastName(updateCommand.getLastName());
        }
        if (updateCommand.getPesel() != null) {
            employee.setPesel(updateCommand.getPesel());
        }
        if (updateCommand.getHeight() != null) {
            employee.setHeight(updateCommand.getHeight());
        }
        if (updateCommand.getWeight() != null) {
            employee.setWeight(updateCommand.getWeight());
        }
        if (updateCommand.getEmail() != null) {
            employee.setEmail(updateCommand.getEmail());
        }
    }

    private PersonDto convertToPersonDto(Employee employee) {
        EmployeeDto employeeDto = EmployeeDto.fromEntity(employee);
        personValidator.validate(employeeDto);
        personRepository.save(employee);
        return employeeDto;
    }
}