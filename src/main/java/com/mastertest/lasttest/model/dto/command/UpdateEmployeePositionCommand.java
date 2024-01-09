package com.mastertest.lasttest.model.dto.command;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEmployeePositionCommand {

    @NotBlank
    private String positionName;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Double salary;

}
