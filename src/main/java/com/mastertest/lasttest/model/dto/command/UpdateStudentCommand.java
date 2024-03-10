package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentCommand   {

    @Pattern(regexp = "^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$", message = "PATTERN_MISMATCH_{regexp}")
    private String universityName;
    @Min(value = 1, message = "Year of study must be at least 1")
    private Integer yearOfStudy;
    @Pattern(regexp = "^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$", message = "PATTERN_MISMATCH_{regexp}")
    private String studyField;
    @PositiveOrZero(message = "Scholarship cannot be negative")
    private Double scholarship;
}
