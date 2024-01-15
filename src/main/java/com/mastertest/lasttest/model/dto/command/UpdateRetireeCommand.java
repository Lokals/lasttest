package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateRetireeCommand extends UpdatePersonCommand {

    @PositiveOrZero(message = "Pension cannot be negative")
    private Double pensionAmount;
    @Min(value = 1, message = "Worked years must be at least 1")
    private Integer yearsWorked;

}
