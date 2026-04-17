package com.backandwhite.infrastructure.client.cj.client;

import static com.backandwhite.domain.exception.Message.EXTERNAL_SERVICE_TOKEN_ERROR;

import com.backandwhite.domain.exception.ExternalServiceException;
import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenDataDto;
import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenRequestDto;
import com.backandwhite.infrastructure.client.cj.dto.CjApiResponseDto;
import com.backandwhite.infrastructure.client.cj.dto.CjRefreshTokenRequestDto;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Dedicated client for CJ Dropshipping authentication endpoints. Separated from
 * CjDropshippingClient to break the circular dependency with CjTokenManager.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class CjAuthClient {

    private static final Duration AUTH_TIMEOUT = Duration.ofSeconds(15);

    private final WebClient cjWebClient;
    private final CjDropshippingProperties properties;

    /**
     * Requests a new token using the apiKey.
     */
    public CjAccessTokenDataDto requestNewToken() {
        log.info("Requesting CJ Dropshipping access token...");

        try {
            CjApiResponseDto<CjAccessTokenDataDto> response = cjWebClient.post().uri("/authentication/getAccessToken")
                    .bodyValue(new CjAccessTokenRequestDto(properties.getApiKey())).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjAccessTokenDataDto>>() {
                    }).timeout(AUTH_TIMEOUT).block();

            if (response == null || response.getData() == null) {
                throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping");
            }

            log.info("CJ access token obtained. Expiry: {}", response.getData().getAccessTokenExpiryDate());
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ token request failed (WebClient): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping");
        } catch (Exception e) {
            log.error("CJ token request failed (unexpected): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping");
        }
    }

    /**
     * Refreshes the access token using the refreshToken.
     */
    public CjAccessTokenDataDto refreshAccessToken(String refreshToken) {
        log.info("Refreshing CJ Dropshipping access token...");

        try {
            CjApiResponseDto<CjAccessTokenDataDto> response = cjWebClient.post()
                    .uri("/authentication/refreshAccessToken").bodyValue(new CjRefreshTokenRequestDto(refreshToken))
                    .retrieve().bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjAccessTokenDataDto>>() {
                    }).timeout(AUTH_TIMEOUT).block();

            if (response == null || response.getData() == null) {
                log.warn("Refresh token failed, will fall back to requesting new token");
                throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping (refresh)");
            }

            log.info("CJ access token refreshed. New expiry: {}", response.getData().getAccessTokenExpiryDate());
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ token refresh failed (WebClient): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping (refresh)");
        } catch (Exception e) {
            log.error("CJ token refresh failed (unexpected): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping (refresh)");
        }
    }
}
