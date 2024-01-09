package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
public class CreatePersonCommand {

    @NotBlank
    private String type;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String pesel;
    @NotNull
    private Double height;
    @NotNull
    private Double weight;
    @Email
    private String email;

    private Map<String, Object> additionalAttributes;

    public <T> Optional<T> getAdditionalAttribute(String key, Class<T> type) {
        if (additionalAttributes == null) {
            return Optional.empty();
        }
        Object value = additionalAttributes.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }
}
