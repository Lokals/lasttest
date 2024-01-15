package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePersonCommand<T> {

    @NotBlank
    private String type;

    @Valid
    private T details;
}
