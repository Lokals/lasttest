package com.mastertest.lasttest.service.employee;

import com.mastertest.lasttest.model.dto.EmployeePositionDto;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;

public interface EmployeePositionService {

    EmployeePositionDto updatePositionToEmployee(Long employeeId, UpdateEmployeePositionCommand command);
}
