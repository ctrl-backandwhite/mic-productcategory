package com.backandwhite.domain.exception;

import java.util.List;

public class EntityNotFoundException extends BaseException {

    public EntityNotFoundException(String code, List<String> detail) {
        super(code, detail);
    }

    public EntityNotFoundException(String code, String message) {
        super(code, message);
    }

    public EntityNotFoundException(String message, String code, List<String> detail) {
        super(message, code, detail);
    }
}
