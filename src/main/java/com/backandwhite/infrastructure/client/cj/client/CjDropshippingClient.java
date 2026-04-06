package com.backandwhite.infrastructure.client.cj.client;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.infrastructure.client.cj.dto.CjApiResponseDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.domain.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;
import java.util.List;

import static com.backandwhite.domain.exception.Message.EXTERNAL_SERVICE_DATA_ERROR;

@Log4j2
@Component
@RequiredArgsConstructor
public class CjDropshippingClient implements DropshippingPort {

    private static final Duration DATA_TIMEOUT = Duration.ofSeconds(30);
    private static final String RESILIENCE4J_INSTANCE = "cjApi";

    private final WebClient cjWebClient;
    private final CjTokenManager cjTokenManager;

    /**
     * Obtiene las categorías de CJ.
     * El token se resuelve automáticamente desde CjTokenManager.
     */
    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public List<CjCategoryFirstLevelDto> getCategories() {
        log.info("Fetching categories from CJ Dropshipping...");

        String accessToken = cjTokenManager.getValidAccessToken();

        try {
            CjApiResponseDto<List<CjCategoryFirstLevelDto>> response = cjWebClient.get()
                    .uri("/product/getCategory")
                    .header("CJ-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<List<CjCategoryFirstLevelDto>>>() {
                    })
                    .timeout(DATA_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping", "categories");
            }

            log.info("Fetched {} first-level categories from CJ Dropshipping", response.getData().size());
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ API categories call failed (WebClient): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "categories: " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API categories call failed (unexpected): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "categories: " + e.getMessage());
        }
    }

    /**
     * Obtiene el detalle de un producto por su pid.
     * El token se resuelve automáticamente desde CjTokenManager.
     */
    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductDetailDto getProductDetail(String pid) {
        log.info("Fetching product detail from CJ Dropshipping for pid: {}", pid);

        String accessToken = cjTokenManager.getValidAccessToken();

        try {
            CjApiResponseDto<CjProductDetailDto> response = cjWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/product/query")
                            .queryParam("pid", pid)
                            .build())
                    .header("CJ-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductDetailDto>>() {
                    })
                    .timeout(DATA_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                        "product detail pid=" + pid);
            }

            log.info("Fetched product detail: pid={}, name={}", pid, response.getData().getProductNameEn());
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ API product detail failed for pid={} (WebClient): {}", pid, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "product detail pid=" + pid + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API product detail failed for pid={} (unexpected): {}", pid, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "product detail pid=" + pid + ": " + e.getMessage());
        }
    }

    /**
     * Obtiene una página de productos de CJ usando el endpoint listV2.
     * El token se resuelve automáticamente desde CjTokenManager.
     */
    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductListPageDto getProductList(int page, int size) {
        log.info("Fetching product list from CJ Dropshipping: page={}, size={}", page, size);

        String accessToken = cjTokenManager.getValidAccessToken();

        try {
            CjApiResponseDto<CjProductListPageDto> response = cjWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/product/listV2")
                            .queryParam("pageNum", page)
                            .queryParam("pageSize", size)
                            .build())
                    .header("CJ-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductListPageDto>>() {
                    })
                    .timeout(DATA_TIMEOUT)
                    .block();

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
        } catch (WebClientException e) {
            log.error("CJ API product list failed for page={} (WebClient): {}", page, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "product list page=" + page + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API product list failed for page={} (unexpected): {}", page, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "product list page=" + page + ": " + e.getMessage());
        }
    }
}
