package com.backandwhite.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void format_substitutesArgs() {
        String out = Message.EXTERNAL_SERVICE_DATA_ERROR.format("CJ", "timeout");
        assertThat(out).contains("CJ").contains("timeout");
    }

    @Test
    void getCode_returnsCode() {
        assertThat(Message.EXTERNAL_SERVICE_TOKEN_ERROR.getCode()).isEqualTo("PR001");
        assertThat(Message.EXTERNAL_SERVICE_DATA_ERROR.getCode()).isEqualTo("PR002");
        assertThat(Message.EXTERNAL_SERVICE_RATE_LIMIT.getCode()).isEqualTo("PR003");
    }

    @Test
    void getDetail_returnsTemplate() {
        assertThat(Message.EXTERNAL_SERVICE_TOKEN_ERROR.getDetail()).contains("%s");
    }

    @Test
    void toExternalServiceException_returnsException() {
        ExternalServiceException ex = Message.EXTERNAL_SERVICE_RATE_LIMIT.toExternalServiceException("CJ");
        assertThat(ex).isNotNull();
        assertThat(ex.getCode()).isEqualTo("PR003");
        assertThat(ex.getMessage()).contains("CJ");
    }
}
