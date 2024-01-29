package com.mastertest.lasttest.model;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@Entity
public class Student extends Person {

    @NotBlank(message = "University name cannot be blank")
    private String universityName;
    @NotNull(message = "Year of study cannot be null")
    @Min(value = 1, message = "Year of study must be at least 1")
    private Integer yearOfStudy;
    @NotBlank(message = "Study field cannot be blank")
    private String studyField;
    @NotNull(message = "Scholarship cannot be null")
    @PositiveOrZero(message = "Scholarship cannot be negative")
    private Double scholarship;
}
