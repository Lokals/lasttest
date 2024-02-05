package com.mastertest.lasttest.controller;


import com.mastertest.lasttest.model.dto.EmployeePositionDto;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;
import com.mastertest.lasttest.service.employee.EmployeePositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/employees")
@RequiredArgsConstructor
public class EmployeePositionController {

    private final EmployeePositionService employeePositionService;


    @PostMapping("/{employeePesel}/positions")
    public ResponseEntity<EmployeePositionDto> updateEmployeePosition(@PathVariable String employeePesel,
                                                                      @Valid
                                                                      @RequestBody UpdateEmployeePositionCommand command) {
        EmployeePositionDto updatedPosition = employeePositionService.updatePositionToEmployee(employeePesel, command);
        return ResponseEntity.ok(updatedPosition);
    }
}
