package com.backandwhite.domain.exception;

import java.util.List;

public class DomainException extends BaseException {

    public DomainException(String code, List<String> detail) {
        super(code, detail);
    }

    public DomainException(String code, String message) {
        super(code, message);
    }

    public DomainException(String message, String code, List<String> detail) {
        super(message, code, detail);
    }
}
