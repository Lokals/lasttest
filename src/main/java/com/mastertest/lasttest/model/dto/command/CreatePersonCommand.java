package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;
import java.util.Optional;

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
