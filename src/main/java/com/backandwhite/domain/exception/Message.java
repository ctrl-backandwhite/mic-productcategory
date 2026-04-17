package com.backandwhite.domain.exception;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Domain-specific messages for CJ Dropshipping integration. For generic
 * messages (ENTITY_NOT_FOUND, VALIDATION_ERROR, etc.) use
 * {@link com.backandwhite.common.exception.Message} from core.
 */
@Log4j2
@Getter
public enum Message {

    EXTERNAL_SERVICE_TOKEN_ERROR("PR001", "Failed to obtain access token from %s"), EXTERNAL_SERVICE_DATA_ERROR("PR002",
            "Failed to fetch data from %s: %s"), EXTERNAL_SERVICE_RATE_LIMIT("PR003",
                    "Too many requests to %s. Please try again later.");

    private final String code;
    private final String detail;

    Message(String code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public String format(Object... args) {
        return String.format(this.detail, args);
    }

    public ExternalServiceException toExternalServiceException(Object... args) {
        log.error("External service error: {}", format(args));
        return new ExternalServiceException(this.code, format(args));
    }
}
