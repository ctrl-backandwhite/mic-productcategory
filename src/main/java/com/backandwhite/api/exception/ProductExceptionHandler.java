package com.backandwhite.api.exception;

import com.backandwhite.api.dto.ApiResponseDtoOut;
import com.backandwhite.domain.exception.ExternalServiceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;
import java.util.Collections;

/**
 * Handlers specific to the productcategory service.
 * Runs before the core GlobalExceptionHandler.
 */
@Log4j2
@Order(1)
@RestControllerAdvice
public class ProductExceptionHandler {

    private static final String RATE_LIMIT_CODE = "ES003";

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponseDtoOut<?>> handleExternalServiceException(
            ExternalServiceException ex,
            WebRequest request) {

        log.warn("External service error: {} - Code: {}", ex.getMessage(), ex.getCode(), ex);

        HttpStatus status = RATE_LIMIT_CODE.equals(ex.getCode())
                ? HttpStatus.TOO_MANY_REQUESTS
                : HttpStatus.BAD_GATEWAY;

        ApiResponseDtoOut<?> response = ApiResponseDtoOut.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .details(ex.getDetail() != null ? ex.getDetail() : Collections.emptyList())
                .timestamp(ZonedDateTime.now())
                .build();

        return new ResponseEntity<>(response, status);
    }
}
