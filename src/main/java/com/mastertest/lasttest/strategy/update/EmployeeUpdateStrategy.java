package com.mastertest.lasttest.strategy.update;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.persons.Employee;
import com.mastertest.lasttest.model.dto.EmployeeDto;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeeCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.repository.EmployeeRepository;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@RequiredArgsConstructor
@Component
public class EmployeeUpdateStrategy implements UpdateStrategy<EmployeeDto, UpdateEmployeeCommand> {

    private final EmployeeRepository employeeRepository;
    private final PersonValidator validator;


    @Override
    public EmployeeDto updateAndValidate(UpdatePersonCommand<UpdateEmployeeCommand> updatePersonCommand, String pesel) throws Exception {
        UpdateEmployeeCommand employeeCommand = ConversionUtils.convertCommandToCommand(updatePersonCommand.getDetails(), UpdateEmployeeCommand.class);

        Employee employee = employeeRepository.findById(pesel).orElseThrow(() -> new EntityNotFoundException(
                MessageFormat.format("person with pesel={0} not found", pesel)));

        if (updatePersonCommand.getFirstName() != null) {
            employee.setFirstName(updatePersonCommand.getFirstName());
        }
        if (updatePersonCommand.getLastName() != null) {
            employee.setLastName(updatePersonCommand.getLastName());
        }
        if (updatePersonCommand.getHeight() != null) {
            employee.setHeight(updatePersonCommand.getHeight());
        }
        if (updatePersonCommand.getWeight() != null) {
            employee.setWeight(updatePersonCommand.getWeight());
        }
        if (updatePersonCommand.getEmail() != null) {
            employee.setEmail(updatePersonCommand.getEmail());
        }
        if (employeeCommand != null) {
            validator.validate(employeeCommand);
            if (employeeCommand.getPosition() != null) {
                employee.setPosition(employeeCommand.getPosition());
            }
            if (employeeCommand.getSalary() != null) {
                employee.setSalary(employeeCommand.getSalary());
            }
            if (employeeCommand.getEmploymentDate() != null) {
                employee.setEmploymentDate(employeeCommand.getEmploymentDate());
            }
        }
        employeeRepository.save(employee);
        return EmployeeDto.fromEntity(employee);

    }
}
