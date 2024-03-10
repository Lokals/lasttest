package com.mastertest.lasttest.model.dto.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeCommand  {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date employmentDate;
    @Pattern(regexp = "^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$", message = "PATTERN_MISMATCH_{regexp}")
    private String position;
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;


}
