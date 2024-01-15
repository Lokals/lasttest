package com.mastertest.lasttest.model.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {

    @NotBlank(message = "First name cannot be blank")
    protected String firstName;

    @NotBlank(message = "Last name cannot be blank")
    protected String lastName;
    @NotBlank(message = "PESEL cannot be blank")
    @Size(min = 11, max = 11, message = "PESEL must be exactly 11 characters long")
    protected String pesel;
    @NotNull(message = "Height cannot be null")
    @Positive(message = "Height must be positive")
    protected Double height;

    @NotNull(message = "Weight cannot be null")
    @Positive(message = "Weight must be positive")
    protected Double weight;

    @Email(message = "Invalid email format")
    protected String email;
}
