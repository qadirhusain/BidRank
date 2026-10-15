package outbid.clone.submission.dto;

import lombok.Getter;
import lombok.Setter;
import outbid.clone.submission.entity.ListingType;
import outbid.clone.submission.entity.SubmissionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SubmissionResponse {

    private UUID id;
    private String title;
    private String description;
    private String url;
    private String logoUrl;
    private ListingType listingType;
    private UUID categoryId;
    private UUID productId;
    private SubmissionStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}