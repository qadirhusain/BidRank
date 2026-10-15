package outbid.clone.search.service;

import lombok.RequiredArgsConstructor;
import outbid.clone.search.client.ProductClient;
import outbid.clone.search.dto.SearchPageResponse;
import outbid.clone.search.dto.SearchResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductClient productClient;

    @Override
    public SearchPageResponse searchProducts(String query, UUID categoryId, int page, int size) {

        List<SearchResponse> products = categoryId != null
                ? productClient.getProductsByCategory(categoryId)
                : productClient.getAllProducts();

        List<String> terms = toTerms(query);

        if (!terms.isEmpty()) {
            products = products.stream()
                    .filter(product -> matchesAll(product, terms))
                    .toList();
        }

        int totalElements = products.size();
        long start = (long) page * size;

        if (start >= totalElements) {
            return SearchPageResponse.of(List.of(), page, size, totalElements);
        }

        int from = (int) start;
        int to = Math.min(from + size, totalElements);

        return SearchPageResponse.of(
                List.copyOf(products.subList(from, to)
                ), page, size, totalElements);

    }

    private List<String> toTerms(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return Arrays.asList(query.trim().toLowerCase(Locale.ROOT).split("\\s+"));
    }

    private boolean matchesAll(SearchResponse product, List<String> terms) {
        String searchable = String.join(" ",
                        nullToEmpty(product.getName()),
                        nullToEmpty(product.getDescription()),
                        nullToEmpty(product.getWebsiteUrl()))
                .toLowerCase(Locale.ROOT);

        return terms.stream().allMatch(searchable::contains);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}