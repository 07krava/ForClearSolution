package com.forclearsolution.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleHttpMessageNotReadable() {

        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid date format");

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleHttpMessageNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid date of birth format. Please use YYYY-MM-DD format.", responseEntity.getBody());
    }

    @Test
    void testDateTimeParseException() {

        DateTimeParseException exception = new DateTimeParseException("Invalid date format", "", 0);

        ResponseEntity<Object> responseEntity = globalExceptionHandler.DateTimeParseException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid date of birth format. Please use YYYY-MM-DD format.", responseEntity.getBody());
    }
}
