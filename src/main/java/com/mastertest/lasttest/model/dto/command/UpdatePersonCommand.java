package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonCommand<T> {

    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    protected String firstName;
    @Pattern(regexp = "^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$", message = "PATTERN_MISMATCH_{regexp}")
    protected String lastName;
    @Size(min = 11, max = 11, message = "Pesel have to have exactly 11 digits")
    @Pattern(regexp = "^[0-9]+$", message = "Pesel can have only digits")
    protected String pesel;
    @Positive(message = "Height must be positive")
    protected Double height;
    @Positive(message = "Weight must be positive")
    protected Double weight;
    @Email
    protected String email;

    @Valid
    private T details;

}
