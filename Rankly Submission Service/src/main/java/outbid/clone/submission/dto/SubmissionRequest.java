package outbid.clone.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import outbid.clone.submission.entity.ListingType;

import java.util.UUID;

@Getter
@Setter
public class SubmissionRequest {

    @NotBlank(message = "Product title is required")
    @Size(max = 150, message = "Product title must not exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Product description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Product URL is required")
    @Size(max = 500, message = "Product URL must not exceed 500 characters")
    @Pattern(
            regexp = "^https?://\\S+$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Product URL must start with http:// or https://"
    )
    private String url;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    @Pattern(
            regexp = "^$|^https?://\\S+$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Logo URL must start with http:// or https://"
    )
    private String logoUrl;

    @NotNull(message = "Listing type is required")
    private ListingType listingType;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}