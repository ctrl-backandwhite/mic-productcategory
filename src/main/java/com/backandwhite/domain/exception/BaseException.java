package com.backandwhite.domain.exception;

import lombok.Data;
import lombok.With;
import lombok.Builder;
import java.util.List;

@Data
@With
@Builder
public class BaseException extends RuntimeException {

    private String code;
    private List<String> detail;

    public BaseException(String code, List<String> detail) {
        this.code = code;
        this.detail = detail;
    }

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message, String code, List<String> detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }
}
