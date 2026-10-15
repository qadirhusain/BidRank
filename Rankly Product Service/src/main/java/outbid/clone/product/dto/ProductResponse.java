package outbid.clone.product.dto;

import lombok.Getter;
import lombok.Setter;
import outbid.clone.product.entity.ListingType;
import outbid.clone.product.entity.ProductStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProductResponse {

    private UUID id;
    private String title;
    private String description;
    private ListingType listingType;
    private String url;
    private UUID categoryId;
    private String logoUrl;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
