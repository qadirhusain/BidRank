package outbid.clone.search.service;

import outbid.clone.search.dto.SearchPageResponse;

import java.util.UUID;

public interface SearchService {

    SearchPageResponse searchProducts(
            String query,
            UUID categoryId,
            int page,
            int size
    );
}