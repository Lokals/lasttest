package com.mastertest.lasttest.model.persons;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@DiscriminatorValue("employee")
public class Employee extends Person {

    private Date employmentDate;
    @Column(unique = true, nullable = false)
    private String position;
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;
}
