package com.backandwhite.infrastructure.client.cj.client;

import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenDataDto;
import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenRequestDto;
import com.backandwhite.infrastructure.client.cj.dto.CjApiResponseDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjRefreshTokenRequestDto;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import com.backandwhite.domain.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static com.backandwhite.domain.exception.Message.EXTERNAL_SERVICE_TOKEN_ERROR;
import static com.backandwhite.domain.exception.Message.EXTERNAL_SERVICE_DATA_ERROR;

@Log4j2
@Component
public class CjDropshippingClient {

        private final RestClient cjRestClient;
        private final CjDropshippingProperties properties;
        private final CjTokenManager cjTokenManager;

        public CjDropshippingClient(RestClient cjRestClient,
                        CjDropshippingProperties properties,
                        @Lazy CjTokenManager cjTokenManager) {
                this.cjRestClient = cjRestClient;
                this.properties = properties;
                this.cjTokenManager = cjTokenManager;
        }

        /**
         * Obtiene un token NUEVO usando la apiKey.
         */
        public CjAccessTokenDataDto requestNewToken() {
                log.info("Requesting CJ Dropshipping access token...");

                try {
                        CjApiResponseDto<CjAccessTokenDataDto> response = cjRestClient.post()
                                        .uri("/authentication/getAccessToken")
                                        .body(new CjAccessTokenRequestDto(properties.getApiKey()))
                                        .retrieve()
                                        .body(new ParameterizedTypeReference<>() {
                                        });

                        if (response == null || response.getData() == null) {
                                throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping");
                        }

                        log.info("CJ Dropshipping access token obtained successfully. Expiry: {}",
                                        response.getData().getAccessTokenExpiryDate());
                        return response.getData();
                } catch (ExternalServiceException e) {
                        throw e;
                } catch (RestClientException e) {
                        log.error("CJ API token request failed (RestClient): {}", e.getMessage(), e);
                        throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping");
                } catch (Exception e) {
                        log.error("CJ API token request failed (unexpected): {}", e.getMessage(), e);
                        throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping");
                }
        }

        /**
         * Refresca el access token usando el refreshToken.
         */
        public CjAccessTokenDataDto refreshAccessToken(String refreshToken) {
                log.info("Refreshing CJ Dropshipping access token...");

                try {
                        CjApiResponseDto<CjAccessTokenDataDto> response = cjRestClient.post()
                                        .uri("/authentication/refreshAccessToken")
                                        .body(new CjRefreshTokenRequestDto(refreshToken))
                                        .retrieve()
                                        .body(new ParameterizedTypeReference<>() {
                                        });

                        if (response == null || response.getData() == null) {
                                log.warn("Refresh token failed, will fall back to requesting new token");
                                throw EXTERNAL_SERVICE_TOKEN_ERROR
                                                .toExternalServiceException("CJ Dropshipping (refresh)");
                        }

                        log.info("CJ Dropshipping access token refreshed successfully. New expiry: {}",
                                        response.getData().getAccessTokenExpiryDate());
                        return response.getData();
                } catch (ExternalServiceException e) {
                        throw e;
                } catch (RestClientException e) {
                        log.error("CJ API token refresh failed (RestClient): {}", e.getMessage(), e);
                        throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping (refresh)");
                } catch (Exception e) {
                        log.error("CJ API token refresh failed (unexpected): {}", e.getMessage(), e);
                        throw EXTERNAL_SERVICE_TOKEN_ERROR.toExternalServiceException("CJ Dropshipping (refresh)");
                }
        }

        /**
         * Obtiene las categorías de CJ. El token se resuelve automáticamente
         * desde el CjTokenManager compartido.
         */
        public List<CjCategoryFirstLevelDto> getCategories() {
                log.info("Fetching categories from CJ Dropshipping...");

                String accessToken = cjTokenManager.getValidAccessToken();

                try {
                        CjApiResponseDto<List<CjCategoryFirstLevelDto>> response = cjRestClient.get()
                                        .uri("/product/getCategory")
                                        .header("CJ-Access-Token", accessToken)
                                        .retrieve()
                                        .body(new ParameterizedTypeReference<>() {
                                        });

                        if (response == null || response.getData() == null) {
                                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                                "categories");
                        }

                        log.info("Fetched {} first-level categories from CJ Dropshipping", response.getData().size());
                        return response.getData();
                } catch (ExternalServiceException e) {
                        throw e;
                } catch (RestClientException e) {
                        log.error("CJ API categories call failed (RestClient): {}", e.getMessage(), e);
                        throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                        "categories: " + e.getMessage());
                } catch (Exception e) {
                        log.error("CJ API categories call failed (unexpected): {}", e.getMessage(), e);
                        throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                        "categories: " + e.getMessage());
                }
        }

        /**
         * Obtiene el detalle de un producto de CJ por su pid.
         * El token se resuelve automáticamente desde el CjTokenManager compartido.
         */
        public CjProductDetailDto getProductDetail(String pid) {
                log.info("Fetching product detail from CJ Dropshipping for pid: {}", pid);

                String accessToken = cjTokenManager.getValidAccessToken();

                try {
                        CjApiResponseDto<CjProductDetailDto> response = cjRestClient.get()
                                        .uri(uriBuilder -> uriBuilder
                                                        .path("/product/query")
                                                        .queryParam("pid", pid)
                                                        .build())
                                        .header("CJ-Access-Token", accessToken)
                                        .retrieve()
                                        .body(new ParameterizedTypeReference<>() {
                                        });

                        if (response == null || response.getData() == null) {
                                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                                "product detail pid=" + pid);
                        }

                        log.info("Fetched product detail from CJ Dropshipping: pid={}, name={}", pid,
                                        response.getData().getProductNameEn());
                        return response.getData();
                } catch (ExternalServiceException e) {
                        throw e;
                } catch (RestClientException e) {
                        log.error("CJ API product detail call failed for pid={} (RestClient): {}", pid, e.getMessage(),
                                        e);
                        throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                        "product detail pid=" + pid + ": " + e.getMessage());
                } catch (Exception e) {
                        log.error("CJ API product detail call failed for pid={} (unexpected): {}", pid, e.getMessage(),
                                        e);
                        throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                        "product detail pid=" + pid + ": " + e.getMessage());
                }
        }

        /**
         * Obtiene una página de productos de CJ Dropshipping usando el endpoint listV2.
         * El token se resuelve automáticamente desde el CjTokenManager compartido.
         */
        public CjProductListPageDto getProductList(int page, int size) {
                log.info("Fetching product list from CJ Dropshipping: page={}, size={}", page, size);

                String accessToken = cjTokenManager.getValidAccessToken();

                try {
                        CjApiResponseDto<CjProductListPageDto> response = cjRestClient.get()
                                        .uri(uriBuilder -> uriBuilder
                                                        .path("/product/listV2")
                                                        .queryParam("pageNum", page)
                                                        .queryParam("pageSize", size)
                                                        .build())
                                        .header("CJ-Access-Token", accessToken)
                                        .retrieve()
                                        .body(new ParameterizedTypeReference<>() {
                                        });

                        log.info("CJ product list raw response: code={}, result={}, message={}, dataIsNull={}",
                                        response != null ? response.getCode() : "null",
                                        response != null ? response.getResult() : "null",
                                        response != null ? response.getMessage() : "null",
                                        response == null || response.getData() == null);

                        if (response == null || response.getData() == null) {
                                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                                "product list page=" + page);
                        }

                        log.info("Fetched {} products from CJ Dropshipping (page {}/total {})",
                                        response.getData().getList() != null ? response.getData().getList().size() : 0,
                                        page, response.getData().getTotal());
                        return response.getData();
                } catch (ExternalServiceException e) {
                        throw e;
                } catch (RestClientException e) {
                        log.error("CJ API product list call failed for page={} (RestClient): {}", page, e.getMessage(),
                                        e);
                        throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                        "product list page=" + page + ": " + e.getMessage());
                } catch (Exception e) {
                        log.error("CJ API product list call failed for page={} (unexpected): {}", page, e.getMessage(),
                                        e);
                        throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                                        "product list page=" + page + ": " + e.getMessage());
                }
        }
}
