package com.mastertest.lasttest.service.employee;

import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.EmployeePosition;
import com.mastertest.lasttest.model.dto.EmployeePositionDto;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;
import com.mastertest.lasttest.repository.EmployeePositionRepository;
import com.mastertest.lasttest.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Service
public class EmployeePositionServiceImpl implements EmployeePositionService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeePositionServiceImpl.class);

    private final EmployeePositionRepository employeePositionRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public EmployeePositionDto updatePositionToEmployee(String employeePesel, UpdateEmployeePositionCommand command) {
        logger.debug("Updating position for employee with pesel: {}", employeePesel);

        Employee employee = getEmployeeByPesel(employeePesel);

        EmployeePosition employeePosition = new EmployeePosition();
        employeePosition.setEmployee(employee);
        employeePosition.setPositionName(command.getPositionName());
        employeePosition.setStartDate(command.getStartDate());
        employeePosition.setEndDate(command.getEndDate());
        employeePosition.setSalary(command.getSalary());

        validatePositionDates(employeePosition);
        if (isPositionOccupied(employeePosition)) {
            throw new IllegalArgumentException("The position " + command.getPositionName() + " is already occupied during the specified period.");
        }
        if (isPositionDatesOverlapWithExisting(employee, employeePosition)) {
            throw new IllegalArgumentException("The position dates overlap with existing positions.");
        }

        employee.setPosition(command.getPositionName());
        employee.setSalary(command.getSalary());
        employeeRepository.save(employee);
        employeePositionRepository.save(employeePosition);
        logger.info("Position updated for employee with pesel: {}", employeePesel);
        return EmployeePositionDto.fromEntity(employeePosition);
    }


    private boolean isPositionDatesOverlapWithExisting(Employee employee, EmployeePosition newEmployeePosition) {
        List<EmployeePosition> existingPositions = employeePositionRepository.findByEmployeePesel(employee.getPesel());
        return existingPositions.stream()
                .anyMatch(position -> position.getId() != newEmployeePosition.getId()
                        && !position.getEndDate().isBefore(newEmployeePosition.getStartDate())
                        && !position.getStartDate().isAfter(newEmployeePosition.getEndDate()));
    }

    private void validatePositionDates(EmployeePosition newEmployeePosition) {
        LocalDate newStartDate = newEmployeePosition.getStartDate();
        LocalDate newEndDate = newEmployeePosition.getEndDate();

        if (newStartDate == null || newEndDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null.");
        }
        if (newStartDate.isAfter(newEndDate)) {
            throw new IllegalArgumentException("The start date of the position cannot be later than the end date.");
        }
    }

    private boolean isPositionOccupied(EmployeePosition newEmployeePosition) {
        List<EmployeePosition> positions = employeePositionRepository.findByPositionName(newEmployeePosition.getPositionName());
        return positions.stream()
                .anyMatch(position -> !position.getEmployee().getPesel().equals(newEmployeePosition.getEmployee().getPesel())
                        && isDateRangeOverlap(newEmployeePosition.getStartDate(), newEmployeePosition.getEndDate(),
                        position.getStartDate(), position.getEndDate()));
    }

    private boolean isDateRangeOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    private Employee getEmployeeByPesel(String employeePesel) {
        logger.debug("Retrieving employee with pesel: {}", employeePesel);
        return employeeRepository.findById(employeePesel)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Employee with pesel={0} not found", employeePesel)));
    }
}