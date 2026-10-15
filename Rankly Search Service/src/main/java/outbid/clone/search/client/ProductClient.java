package outbid.clone.search.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import outbid.clone.search.dto.SearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import outbid.clone.search.exception.ProductServiceUnavailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ProductClient {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 50;

    private final RestClient restClient;

    public ProductClient(@Qualifier("productRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SearchResponse> getAllProducts() {
        List<SearchResponse> all = new ArrayList<>();
        try {
            for (int page = 0; page < MAX_PAGES; page++) {
                ProductPage result = restClient.get()
                        .uri("/api/products?page={page}&size={size}", page, PAGE_SIZE)
                        .retrieve()
                        .body(ProductPage.class);

                if (result == null || result.getContent() == null) break;
                result.getContent().forEach(p -> all.add(toResponse(p)));
                if (result.isLast()) break;
            }
            return all;
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    public List<SearchResponse> getProductsByCategory(UUID categoryId) {
        List<SearchResponse> all = new ArrayList<>();
        try {
            for (int page = 0; page < MAX_PAGES; page++) {
                ProductPage result = restClient.get()
                        .uri("/api/products/category/{categoryId}?page={page}&size={size}",
                                categoryId, page, PAGE_SIZE)
                        .retrieve()
                        .body(ProductPage.class);

                if (result == null || result.getContent() == null) break;
                result.getContent().forEach(p -> all.add(toResponse(p)));
                if (result.isLast()) break;
            }
            return all;
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private SearchResponse toResponse(ProductDto p) {
        return SearchResponse.builder()
                .id(p.getId())
                .name(p.getTitle())
                .description(p.getDescription())
                .websiteUrl(p.getUrl())
                .logoUrl(p.getLogoUrl())
                .categoryId(p.getCategoryId())
                .build();
    }

    private ProductServiceUnavailableException unavailable(RestClientException cause) {
        if (cause instanceof ResourceAccessException) {
            log.warn("Product service unreachable: {}", cause.getMessage());
        } else {
            log.error("Product service replied but the response could not be used", cause);
        }
        return new ProductServiceUnavailableException(
                "Product service is currently unavailable", cause);
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ProductDto {
        private UUID id;
        private String title;
        private String description;
        private String url;
        private String logoUrl;
        private UUID categoryId;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ProductPage {
        private List<ProductDto> content;
        private boolean last;
    }
}