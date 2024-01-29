package com.mastertest.lasttest.model;


import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@Entity
public class Retiree extends Person {
    @NotNull(message = "Pension cannot be null")
    @PositiveOrZero(message = "Pension cannot be negative")
    private Double pensionAmount;
    @NotNull(message = "Worked years cannot be null")
    @Min(value = 1, message = "Worked years must be at least 1")
    private Integer yearsWorked;
}
