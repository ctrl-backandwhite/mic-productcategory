package com.backandwhite.infrastructure.client.cj.client;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.infrastructure.client.cj.dto.CjApiResponseDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryByPidItemDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductCommentsPageDto;
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
     * Fetches categories from CJ.
     * The token is automatically resolved from CjTokenManager.
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
     * Fetches the detail of a product by its pid.
     * The token is automatically resolved from CjTokenManager.
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
     * Fetches a page of CJ products using the listV2 endpoint.
     * The token is automatically resolved from CjTokenManager.
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
                            .queryParam("page", page)
                            .queryParam("size", size)
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
                    response.getData().getAllProducts().size(),
                    page, response.getData().getTotalRecords());
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

    /**
     * Fetches per-variant inventory for the given pid.
     */
    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public List<CjInventoryByPidItemDto> getInventoryByPid(String pid) {
        log.info("Fetching inventory by pid from CJ Dropshipping: pid={}", pid);

        String accessToken = cjTokenManager.getValidAccessToken();

        try {
            CjApiResponseDto<List<CjInventoryByPidItemDto>> response = cjWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/product/stock/getInventoryByPid")
                            .queryParam("pid", pid)
                            .build())
                    .header("CJ-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<List<CjInventoryByPidItemDto>>>() {
                    })
                    .timeout(DATA_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                        "inventory by pid=" + pid);
            }

            log.info("Fetched {} inventory items for pid={}", response.getData().size(), pid);
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ API inventory by pid failed for pid={} (WebClient): {}", pid, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "inventory pid=" + pid + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API inventory by pid failed for pid={} (unexpected): {}", pid, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "inventory pid=" + pid + ": " + e.getMessage());
        }
    }

    /**
     * Fetches a page of product comments (reviews) from CJ.
     */
    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductCommentsPageDto getProductComments(String pid, int score, int page, int size) {
        log.info("Fetching product comments from CJ: pid={}, score={}, page={}, size={}", pid, score, page, size);

        String accessToken = cjTokenManager.getValidAccessToken();

        try {
            CjApiResponseDto<CjProductCommentsPageDto> response = cjWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/product/productComments")
                            .queryParam("pid", pid)
                            .queryParam("score", score)
                            .queryParam("pageNum", page)
                            .queryParam("pageSize", size)
                            .build())
                    .header("CJ-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductCommentsPageDto>>() {
                    })
                    .timeout(DATA_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                        "product comments pid=" + pid);
            }

            log.info("Fetched {} reviews for pid={} (page {})", response.getData().getList().size(), pid, page);
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ API product comments failed for pid={} (WebClient): {}", pid, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "product comments pid=" + pid + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API product comments failed for pid={} (unexpected): {}", pid, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "product comments pid=" + pid + ": " + e.getMessage());
        }
    }

    /**
     * Fetches a filtered page of products from CJ using listV2 endpoint.
     * All filter parameters are optional — pass null to omit from the query.
     */
    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductListPageDto getProductListFiltered(int page, int size, String categoryId,
            String keyword, Long timeStart, Long timeEnd, Integer orderBy, String sort) {
        log.info("Fetching filtered product list: page={}, size={}, categoryId={}, keyword={}, orderBy={}, sort={}",
                page, size, categoryId, keyword, orderBy, sort);

        String accessToken = cjTokenManager.getValidAccessToken();

        try {
            CjApiResponseDto<CjProductListPageDto> response = cjWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/product/listV2")
                                .queryParam("page", page)
                                .queryParam("size", size);
                        if (categoryId != null && !categoryId.isBlank()) {
                            uriBuilder.queryParam("categoryId", categoryId);
                        }
                        if (keyword != null && !keyword.isBlank()) {
                            uriBuilder.queryParam("keyWord", keyword);
                        }
                        if (timeStart != null) {
                            uriBuilder.queryParam("timeStart", timeStart);
                        }
                        if (timeEnd != null) {
                            uriBuilder.queryParam("timeEnd", timeEnd);
                        }
                        if (orderBy != null) {
                            uriBuilder.queryParam("orderBy", orderBy);
                        }
                        if (sort != null && !sort.isBlank()) {
                            uriBuilder.queryParam("sort", sort);
                        }
                        return uriBuilder.build();
                    })
                    .header("CJ-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductListPageDto>>() {
                    })
                    .timeout(DATA_TIMEOUT)
                    .block();

            if (response == null || response.getData() == null) {
                log.warn("CJ returned null data for filtered list: categoryId={}, keyword={}", categoryId, keyword);
                return null;
            }

            log.info("CJ listV2 response: code={}, result={}, products={}, totalRecords={}, pageNumber={}",
                    response.getCode(), response.getResult(),
                    response.getData().getAllProducts().size(),
                    response.getData().getTotalRecords(), response.getData().getPageNumber());
            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ API filtered list failed (WebClient): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "filtered product list: " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API filtered list failed (unexpected): {}", e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException("CJ Dropshipping",
                    "filtered product list: " + e.getMessage());
        }
    }
}
