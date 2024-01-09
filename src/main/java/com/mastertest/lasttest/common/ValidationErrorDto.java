package com.mastertest.lasttest.common;

import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationErrorDto extends ExceptionDto {

    private final List<ViolationInfo> violations = new ArrayList<>();

    public ValidationErrorDto() {
        super("validation errors");
    }

    public void addViolation(String field, String message) {

        violations.add(new ViolationInfo(field, message));
    }

    @Value
    public static class ViolationInfo {
        String field;
        String message;
    }
}
