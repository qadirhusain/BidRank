package outbid.clone.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import outbid.clone.product.entity.ListingType;

import java.util.UUID;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product title is required")
    @Size(max = 150, message = "Product title must not exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Product description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Listing type is required")
    private ListingType listingType;

    @NotBlank(message = "Product URL is required")
    @Size(max = 500, message = "Product URL must not exceed 500 characters")
    private String url;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    private String logoUrl;
}
