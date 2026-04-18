package com.backandwhite.infrastructure.client.cj.client;

import static com.backandwhite.domain.exception.Message.EXTERNAL_SERVICE_DATA_ERROR;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.exception.ExternalServiceException;
import com.backandwhite.infrastructure.client.cj.dto.CjApiResponseDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryByPidItemDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductCommentsPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Log4j2
@Component
@RequiredArgsConstructor
public class CjDropshippingClient implements DropshippingPort {

    private static final Duration DATA_TIMEOUT = Duration.ofSeconds(30);
    private static final String RESILIENCE4J_INSTANCE = "cjApi";
    private static final String CJ_SERVICE = "CJ Dropshipping";
    private static final String HEADER_CJ_TOKEN = "CJ-Access-Token";
    private static final String CTX_PRODUCT_DETAIL = "product detail pid=";
    private static final String CTX_PRODUCT_LIST = "product list page=";
    private static final String CTX_PRODUCT_COMMENTS = "product comments pid=";
    private static final String CTX_INVENTORY = "inventory pid=";
    private static final String CTX_FILTERED_LIST = "filtered product list";

    private final WebClient cjWebClient;
    private final CjTokenManager cjTokenManager;

    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public List<CjCategoryFirstLevelDto> getCategories() {
        log.info("Fetching categories from CJ Dropshipping...");
        String accessToken = cjTokenManager.getValidAccessToken();

        CjApiResponseDto<List<CjCategoryFirstLevelDto>> response = invokeCj(
                () -> cjWebClient.get().uri("/product/getCategory").header(HEADER_CJ_TOKEN, accessToken).retrieve()
                        .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<List<CjCategoryFirstLevelDto>>>() {
                        }).timeout(DATA_TIMEOUT).block(),
                "categories");

        List<CjCategoryFirstLevelDto> data = requireData(response, "categories");
        log.info("Fetched {} first-level categories from CJ Dropshipping", data.size());
        return data;
    }

    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductDetailDto getProductDetail(String pid) {
        log.info("Fetching product detail from CJ Dropshipping for pid: {}", pid);
        String accessToken = cjTokenManager.getValidAccessToken();
        String ctx = CTX_PRODUCT_DETAIL + pid;

        CjApiResponseDto<CjProductDetailDto> response = invokeCj(() -> cjWebClient.get()
                .uri(b -> b.path("/product/query").queryParam("pid", pid).build()).header(HEADER_CJ_TOKEN, accessToken)
                .retrieve().bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductDetailDto>>() {
                }).timeout(DATA_TIMEOUT).block(), ctx);

        CjProductDetailDto data = requireData(response, ctx);
        log.info("Fetched product detail: pid={}, name={}", pid, data.getProductNameEn());
        return data;
    }

    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductListPageDto getProductList(int page, int size) {
        log.info("Fetching product list from CJ Dropshipping: page={}, size={}", page, size);
        String accessToken = cjTokenManager.getValidAccessToken();
        String ctx = CTX_PRODUCT_LIST + page;

        CjApiResponseDto<CjProductListPageDto> response = invokeCj(() -> cjWebClient.get()
                .uri(b -> b.path("/product/listV2").queryParam("page", page).queryParam("size", size).build())
                .header(HEADER_CJ_TOKEN, accessToken).retrieve()
                .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductListPageDto>>() {
                }).timeout(DATA_TIMEOUT).block(), ctx);

        log.info("CJ product list raw response: code={}, result={}, message={}, dataIsNull={}",
                response != null ? response.getCode() : "null", response != null ? response.getResult() : "null",
                response != null ? response.getMessage() : "null", response == null || response.getData() == null);

        CjProductListPageDto data = requireData(response, ctx);
        log.info("Fetched {} products from CJ Dropshipping (page {}/total {})", data.getAllProducts().size(), page,
                data.getTotalRecords());
        return data;
    }

    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public List<CjInventoryByPidItemDto> getInventoryByPid(String pid) {
        log.info("Fetching inventory by pid from CJ Dropshipping: pid={}", pid);
        String accessToken = cjTokenManager.getValidAccessToken();
        String ctx = CTX_INVENTORY + pid;

        CjApiResponseDto<List<CjInventoryByPidItemDto>> response = invokeCj(() -> cjWebClient.get()
                .uri(b -> b.path("/product/stock/getInventoryByPid").queryParam("pid", pid).build())
                .header(HEADER_CJ_TOKEN, accessToken).retrieve()
                .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<List<CjInventoryByPidItemDto>>>() {
                }).timeout(DATA_TIMEOUT).block(), ctx);

        List<CjInventoryByPidItemDto> data = requireData(response, ctx);
        log.info("Fetched {} inventory items for pid={}", data.size(), pid);
        return data;
    }

    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductCommentsPageDto getProductComments(String pid, int score, int page, int size) {
        log.info("Fetching product comments from CJ: pid={}, score={}, page={}, size={}", pid, score, page, size);
        String accessToken = cjTokenManager.getValidAccessToken();
        String ctx = CTX_PRODUCT_COMMENTS + pid;

        CjApiResponseDto<CjProductCommentsPageDto> response = invokeCj(() -> cjWebClient.get()
                .uri(b -> b.path("/product/productComments").queryParam("pid", pid).queryParam("score", score)
                        .queryParam("pageNum", page).queryParam("pageSize", size).build())
                .header(HEADER_CJ_TOKEN, accessToken).retrieve()
                .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductCommentsPageDto>>() {
                }).timeout(DATA_TIMEOUT).block(), ctx);

        CjProductCommentsPageDto data = requireData(response, ctx);
        log.info("Fetched {} reviews for pid={} (page {})", data.getList().size(), pid, page);
        return data;
    }

    @Override
    @Retry(name = RESILIENCE4J_INSTANCE)
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE)
    public CjProductListPageDto getProductListFiltered(int page, int size, String categoryId, String keyword,
            Long timeStart, Long timeEnd, Integer orderBy, String sort) {
        log.info("Fetching filtered product list: page={}, size={}, categoryId={}, keyword={}, orderBy={}, sort={}",
                page, size, categoryId, keyword, orderBy, sort);

        String accessToken = cjTokenManager.getValidAccessToken();
        ProductListFilters filters = new ProductListFilters(page, size, categoryId, keyword, timeStart, timeEnd,
                orderBy, sort);

        CjApiResponseDto<CjProductListPageDto> response = invokeCj(() -> cjWebClient.get()
                .uri(b -> applyListFilters(b, filters).build()).header(HEADER_CJ_TOKEN, accessToken).retrieve()
                .bodyToMono(new ParameterizedTypeReference<CjApiResponseDto<CjProductListPageDto>>() {
                }).timeout(DATA_TIMEOUT).block(), CTX_FILTERED_LIST);

        if (response == null || response.getData() == null) {
            log.warn("CJ returned null data for filtered list: categoryId={}, keyword={}", categoryId, keyword);
            return null;
        }

        log.info("CJ listV2 response: code={}, result={}, products={}, totalRecords={}, pageNumber={}",
                response.getCode(), response.getResult(), response.getData().getAllProducts().size(),
                response.getData().getTotalRecords(), response.getData().getPageNumber());
        return response.getData();
    }

    private static org.springframework.web.util.UriBuilder applyListFilters(org.springframework.web.util.UriBuilder b,
            ProductListFilters f) {
        b.path("/product/listV2").queryParam("page", f.page()).queryParam("size", f.size());
        applyIfPresent(b, "categoryId", f.categoryId());
        applyIfPresent(b, "keyWord", f.keyword());
        applyIfPresent(b, "timeStart", f.timeStart());
        applyIfPresent(b, "timeEnd", f.timeEnd());
        applyIfPresent(b, "orderBy", f.orderBy());
        applyIfPresent(b, "sort", f.sort());
        return b;
    }

    private static void applyIfPresent(org.springframework.web.util.UriBuilder b, String name, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String s && s.isBlank()) {
            return;
        }
        b.queryParam(name, value);
    }

    private record ProductListFilters(int page, int size, String categoryId, String keyword, Long timeStart,
            Long timeEnd, Integer orderBy, String sort) {
    }

    /**
     * Wraps a CJ WebClient call with unified exception handling. Propagates
     * {@link ExternalServiceException} unchanged and translates any other error
     * into {@link ExternalServiceException} tagged with the provided context.
     */
    private <T> T invokeCj(Supplier<T> call, String context) {
        try {
            return call.get();
        } catch (ExternalServiceException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("CJ API {} failed (WebClient): {}", context, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException(CJ_SERVICE, context + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("CJ API {} failed (unexpected): {}", context, e.getMessage(), e);
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException(CJ_SERVICE, context + ": " + e.getMessage());
        }
    }

    private <T> T requireData(CjApiResponseDto<T> response, String context) {
        if (response == null || response.getData() == null) {
            throw EXTERNAL_SERVICE_DATA_ERROR.toExternalServiceException(CJ_SERVICE, context);
        }
        return response.getData();
    }
}
