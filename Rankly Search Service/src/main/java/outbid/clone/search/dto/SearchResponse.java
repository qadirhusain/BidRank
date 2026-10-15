package outbid.clone.search.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private UUID id;
    private String name;
    private String description;
    private String websiteUrl;
    private String logoUrl;
    private UUID categoryId;
}