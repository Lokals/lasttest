package com.mastertest.lasttest.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleEntityNotFoundException(EntityNotFoundException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ValidationErrorDto errorDto = new ValidationErrorDto();
        exception.getFieldErrors().forEach(fieldError ->
                errorDto.addViolation(fieldError.getField(), fieldError.getDefaultMessage()));
        return errorDto;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ExceptionDto handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return new ExceptionDto("Method not supported: " + exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return new ExceptionDto("Invalid value for parameter: " + exception.getName());
    }

    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleJsonProcessingException(JsonProcessingException exception) {
        return new ExceptionDto("Invalid JSON format or data: " + exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ExceptionDto("Invalid data provided. Please check your data and try again.");
    }

    @ExceptionHandler(JsonMappingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleJsonMappingException(JsonMappingException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(FileProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleFileProcessingException(FileProcessingException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(UnknownEntityTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleUnknownEntityTypeException(UnknownEntityTypeException exception) {
        return new ExceptionDto(exception.getMessage());
    }


    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleOptimisticLockException(OptimisticLockException exception) {
        return new ExceptionDto(exception.getMessage());
    }
}
