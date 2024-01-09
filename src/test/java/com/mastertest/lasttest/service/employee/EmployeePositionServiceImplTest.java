package com.mastertest.lasttest.service.employee;

import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.EmployeePosition;
import com.mastertest.lasttest.model.dto.EmployeePositionDto;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;
import com.mastertest.lasttest.repository.EmployeePositionRepository;
import com.mastertest.lasttest.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EmployeePositionServiceImplTest {

    @InjectMocks
    private EmployeePositionServiceImpl employeePositionService;

    @Mock
    private EmployeePositionRepository employeePositionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private static Employee employee;
    private static UpdateEmployeePositionCommand command;


    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Test");
        employee.setLastName("Test");
        command = new UpdateEmployeePositionCommand();
        command.setPositionName("Tester");
        command.setStartDate(LocalDate.now());
        command.setEndDate(LocalDate.now().plusDays(10));
        command.setSalary(50000.00);
    }

    @Test
    void updatePositionToEmployee_WithValidData_UpdatesPosition() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(employeePositionRepository.findByPositionName(anyString())).thenReturn(Collections.emptyList());

        EmployeePositionDto result = employeePositionService.updatePositionToEmployee(1L, command);

        assertNotNull(result);
        assertEquals("Tester", result.getPositionName());
        verify(employeeRepository).save(any(Employee.class));
        verify(employeePositionRepository).save(any(EmployeePosition.class));

    }

    @Test
    void updatePositionToEmployee_PositionAlreadyOccupied_ThrowsIllegalArgumentException() {
        Employee otherEmployee = new Employee();
        otherEmployee.setId(2L);
        EmployeePosition existingPosition = new EmployeePosition();
        existingPosition.setId(2L);
        existingPosition.setEmployee(otherEmployee);
        existingPosition.setPositionName("Tester");
        existingPosition.setStartDate(LocalDate.now());
        existingPosition.setEndDate(LocalDate.now().plusDays(10));

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(employeePositionRepository.findByPositionName(anyString())).thenReturn(Collections.singletonList(existingPosition));

        assertThrows(IllegalArgumentException.class, () -> employeePositionService.updatePositionToEmployee(1L, command));
    }

    @Test
    void updatePositionToEmployee_InvalidDateRange_ThrowsIllegalArgumentException() {
        command.setEndDate(command.getStartDate().minusDays(1));

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> employeePositionService.updatePositionToEmployee(1L, command));
    }

    @Test
    void updatePositionToEmployee_EmployeeNotFound_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeePositionService.updatePositionToEmployee(1L, command));
    }


}