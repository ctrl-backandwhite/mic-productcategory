package com.backandwhite.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.ApiResponseDtoOut;
import com.backandwhite.domain.exception.ExternalServiceException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

class ProductExceptionHandlerTest {

    private final ProductExceptionHandler handler = new ProductExceptionHandler();

    @Test
    void handleExternalServiceException_rateLimit_returnsTooManyRequests() {
        ExternalServiceException ex = new ExternalServiceException("ES003", "rate limited");
        ResponseEntity<ApiResponseDtoOut<?>> response = handler.handleExternalServiceException(ex,
                Mockito.mock(WebRequest.class));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody().getCode()).isEqualTo("ES003");
    }

    @Test
    void handleExternalServiceException_otherError_returnsBadGateway() {
        ExternalServiceException ex = new ExternalServiceException("ES001", "generic");
        ResponseEntity<ApiResponseDtoOut<?>> response = handler.handleExternalServiceException(ex,
                Mockito.mock(WebRequest.class));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void handleExternalServiceException_withDetailList_includesDetails() {
        ExternalServiceException ex = new ExternalServiceException("ES002", List.of("d1", "d2"));
        ResponseEntity<ApiResponseDtoOut<?>> response = handler.handleExternalServiceException(ex,
                Mockito.mock(WebRequest.class));
        assertThat(response.getBody().getDetails()).containsExactly("d1", "d2");
    }
}
