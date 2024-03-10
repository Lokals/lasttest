package com.mastertest.lasttest.model.dto;


import com.mastertest.lasttest.model.persons.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto extends PersonDto {
    @NotNull(message = "Employment date cannot be null")
    private Date employmentDate;
    @NotBlank(message = "Position cannot be blank")
    private String position;
    @NotNull(message = "Salary cannot be null")
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;

    @Builder
    public EmployeeDto(String firstName,
                       String lastName,
                       String pesel,
                       Double height,
                       Double weight,
                       String email,
                       Date employmentDate,
                       String position,
                       Double salary) {
        super(firstName, lastName, pesel, height, weight, email);
        this.employmentDate = employmentDate;
        this.position = position;
        this.salary = salary;
    }

    public static EmployeeDto fromEntity(Employee employee) {
        return EmployeeDto.builder()
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .pesel(employee.getPesel())
                .height(employee.getHeight())
                .weight(employee.getWeight())
                .email(employee.getEmail())
                .employmentDate(employee.getEmploymentDate())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .build();
    }
}
