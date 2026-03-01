package ru.yandex.practicum.mymarket.controllers;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class ControllerExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);


/*    @Bean
    public RouterFunction<ServerResponse> routes(UserHandler userHandler) {
        return RouterFunctions.route()
                .onError(NoSuchElementException.class, (e, request) -> ServerResponse.status(HttpStatus.NOT_FOUND).build())
                .onError(IllegalArgumentException.class, (e, request) -> ServerResponse.status(HttpStatus.BAD_REQUEST).build())
                .onError(Exception.class, (e, request) -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .build();
    }*/

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleAllErrors(@NotNull final Exception e) {
        logger.error("Unexpected error: ", e);
    }

    @ExceptionHandler({NoSuchFileException.class})
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