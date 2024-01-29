package com.mastertest.lasttest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
public class Employee extends Person {
    @NotNull(message = "Employment date cannot be null")
    private Date employmentDate;
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Position cannot be blank")
    private String position;
    @NotNull(message = "Salary cannot be null")
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;
}
