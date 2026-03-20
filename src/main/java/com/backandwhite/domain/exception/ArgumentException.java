package com.backandwhite.domain.exception;

import java.util.List;

public class ArgumentException extends BaseException {

    public ArgumentException(String code, List<String> detail) {
        super(code, detail);
    }

    public ArgumentException(String code, String message) {
        super(code, message);
    }

    public ArgumentException(String message, String code, List<String> detail) {
        super(message, code, detail);
    }
}
