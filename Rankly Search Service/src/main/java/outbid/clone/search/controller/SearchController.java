package outbid.clone.search.controller;

import lombok.RequiredArgsConstructor;
import outbid.clone.search.dto.SearchPageResponse;
import outbid.clone.search.exception.InvalidSearchException;
import outbid.clone.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final int MAX_QUERY_LENGTH = 100;

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchPageResponse> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        if (q != null && q.length() > MAX_QUERY_LENGTH) {
            throw new InvalidSearchException(
                    "Search query must not exceed " + MAX_QUERY_LENGTH + " characters");
        }

        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        return ResponseEntity.ok(searchService.searchProducts(q, categoryId, safePage, safeSize));
    }
}