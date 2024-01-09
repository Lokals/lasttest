package com.mastertest.lasttest.model.dto;


import com.mastertest.lasttest.model.Employee;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class EmployeeDto  {

    private Date employmentDate;
    private String position;
    private Double salary;


    public static EmployeeDto fromEntity(Employee employee) {
        return EmployeeDto.builder()
                .employmentDate(employee.getEmploymentDate())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .build();
    }
}
