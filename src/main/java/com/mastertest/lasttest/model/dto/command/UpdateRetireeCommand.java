package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRetireeCommand  {

    @PositiveOrZero(message = "Pension cannot be negative")
    private Double pensionAmount;
    @Min(value = 1, message = "Worked years must be at least 1")
    private Integer yearsWorked;

}
