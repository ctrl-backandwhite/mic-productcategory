package com.backandwhite.domain.exception;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public enum Message {

    CREATE_SINGLE("CR001", "The %s has been created successfully."),
    UPDATE_SINGLE("UP001", "The %s has been updated successfully."),
    DELETE_SINGLE("DL002", "The %s has been deleted successfully."),
    CREATE_MULTIPLE("CR002", "The %s have been created successfully."),
    UPDATE_MULTIPLE("UP002", "The %s have been updated successfully."),
    DELETE_MULTIPLE("DL002", "The %s have been deleted successfully."),
    JSON_FORMAT_ERROR("JF001", "The JSON format is invalid."),
    VALIDATION_ERROR("VE001", "One or more validation errors occurred."),
    ENTITY_NOT_FOUND("NF001", "%s with id %s is not found."),
    INVALID_ARGUMENT("IA001", "Invalid or inactive permission"),
    UNASSOCIATED_ARGUMENT("IA002", "There is no %s associated record"),
    EXTERNAL_SERVICE_TOKEN_ERROR("ES001", "Failed to obtain access token from %s"),
    EXTERNAL_SERVICE_DATA_ERROR("ES002", "Failed to fetch data from %s: %s"),
    EXTERNAL_SERVICE_RATE_LIMIT("ES003", "Too many requests to %s. Please try again later.");

    private final String code;
    private final String detail;

    Message(String code, String message) {
        this.code = code;
        this.detail = message;
    }

    public String format(Object... args) {
        return String.format(this.detail, args);
    }

    public EntityNotFoundException toEntityNotFound(Object... args) {
        log.info("{} with id {} is not found.", args);
        return new EntityNotFoundException(this.code, format(args));
    }

    public ArgumentException toArgumentException(Object... args) {
        log.info("{} Invalid or inactive permission", args);
        return new ArgumentException(this.code, format(args));
    }

    public ExternalServiceException toExternalServiceException(Object... args) {
        log.error("External service error: {}", format(args));
        return new ExternalServiceException(this.code, format(args));
    }
}
