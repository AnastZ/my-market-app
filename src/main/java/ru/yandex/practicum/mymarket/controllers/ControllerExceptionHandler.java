package ru.yandex.practicum.mymarket.controllers;

import jakarta.persistence.NoResultException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import jakarta.persistence.NonUniqueResultException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleAllErrors(@NotNull final Exception e) {
        logger.error("Unexpected error: ", e);
    }
    @ExceptionHandler({NoResultException.class, NoSuchFileException.class, NonUniqueResultException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResultException(@NotNull final Exception e) {
        logger.error("Unexpected error: ", e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArgumentException(@NotNull final IllegalArgumentException e) {
        logger.error("Unexpected error: ", e);
    }
}