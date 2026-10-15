package outbid.clone.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "submissions",
        indexes = {
                @Index(name = "idx_submission_category", columnList = "category_id"),
                @Index(name = "idx_submission_status", columnList = "status"),
                @Index(name = "idx_submission_slug", columnList = "slug"),
                @Index(name = "idx_submission_created", columnList = "created_at")
        }
)
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ListingType listingType;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "product_id")
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}