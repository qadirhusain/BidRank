package outbid.clone.submission.dto;

import lombok.*;
import outbid.clone.submission.entity.ListingType;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    private String title;
    private String description;
    private ListingType listingType;
    private String url;
    private UUID categoryId;
    private String logoUrl;
}
