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
@DiscriminatorValue("student")
public class Student extends Person {


    private String universityName;
    @Min(value = 1, message = "Year of study must be at least 1")
    private Integer yearOfStudy;
    private String studyField;
    @PositiveOrZero(message = "Scholarship cannot be negative")
    private Double scholarship;
}
