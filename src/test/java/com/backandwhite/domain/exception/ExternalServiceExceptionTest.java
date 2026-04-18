package com.backandwhite.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExternalServiceExceptionTest {

    @Test
    void constructorWithCodeAndDetailList_setsFields() {
        ExternalServiceException ex = new ExternalServiceException("C1", List.of("d1", "d2"));
        assertThat(ex.getCode()).isEqualTo("C1");
        assertThat(ex.getDetail()).containsExactly("d1", "d2");
    }

    @Test
    void constructorWithCodeAndMessage_setsFields() {
        ExternalServiceException ex = new ExternalServiceException("C2", "message");
        assertThat(ex.getCode()).isEqualTo("C2");
        assertThat(ex.getMessage()).isEqualTo("message");
    }

    @Test
    void constructorWithMessageCodeDetail_setsFields() {
        ExternalServiceException ex = new ExternalServiceException("msg", "C3", List.of("d"));
        assertThat(ex.getCode()).isEqualTo("C3");
        assertThat(ex.getMessage()).isEqualTo("msg");
        assertThat(ex.getDetail()).containsExactly("d");
    }
}
