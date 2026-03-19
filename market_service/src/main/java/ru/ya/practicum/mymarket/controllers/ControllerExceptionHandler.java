package ru.ya.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.ya.practicum.mymarket.model.NotFoundException;

import java.nio.file.NoSuchFileException;


@RestControllerAdvice
public class ControllerExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleAllErrors(@NotNull final Exception e) {
        logger.error("Unexpected error: ", e);
    }

    @ExceptionHandler({NoSuchFileException.class, NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResultException(@NotNull final Exception e) {
        logger.error("Unexpected error: ", e);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArgumentException(@NotNull final Exception e) {
        logger.error("Unexpected error: ", e);
    }
}