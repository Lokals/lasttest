package com.mastertest.lasttest.model.persons;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("retiree")
public class Retiree extends Person {

    @PositiveOrZero(message = "Pension cannot be negative")
    private Double pensionAmount;

    @Min(value = 1, message = "Worked years must be at least 1")
    private Integer yearsWorked;

}
