package com.mastertest.lasttest.model.dto.command;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Map;
import java.util.Optional;

@Data
public class UpdatePersonCommand {

    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String firstName;
    @Pattern(regexp = "^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$", message = "PATTERN_MISMATCH_{regexp}")
    private String lastName;
    @Size(min = 11, max = 11, message = "Pesel have to have exactly 11 digits")
    @Pattern(regexp = "^[0-9]+$", message = "Pesel can have only digits")
    private String pesel;
    @DecimalMin(value = "1.0", inclusive = false, message = "MIN_VALUE_1.0")
    private Double height;
    @DecimalMin(value = "1.0", inclusive = false, message = "MIN_VALUE_1.0")
    private Double weight;
    @Email
    private String email;
    private Map<String, Object> additionalAttributes;

    public <T> Optional<T> getAdditionalAttributes(String key, Class<T> type) {
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
