package com.mastertest.lasttest.validator;

import com.mastertest.lasttest.service.fileprocess.CsvImportServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@AllArgsConstructor
@Component
public class PersonValidator {
    private final Validator validator;
    private static final Logger logger = LoggerFactory.getLogger(PersonValidator.class);


    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("\n");
            }
            logger.error("Validation of entity failed: {}", sb.toString());
            throw new ValidationException(sb.toString());
        }
    }
}
