package outbid.clone.search.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPageResponse {

    private List<SearchResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static SearchPageResponse of(
            List<SearchResponse> content,
            int page,
            int size,
            long totalElements
    ) {

        int totalPages = (int) ((totalElements + size - 1) / size);

        return SearchPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}