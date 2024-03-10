package com.mastertest.lasttest.model.dto;


import com.mastertest.lasttest.model.position.EmployeePosition;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeePositionDto {

    private String positionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double salary;


    public static EmployeePositionDto fromEntity(EmployeePosition employeePosition){
        return EmployeePositionDto.builder()
                .positionName(employeePosition.getPositionName())
                .startDate(employeePosition.getStartDate())
                .endDate(employeePosition.getEndDate())
                .salary(employeePosition.getSalary())
                .build();
    }
}
