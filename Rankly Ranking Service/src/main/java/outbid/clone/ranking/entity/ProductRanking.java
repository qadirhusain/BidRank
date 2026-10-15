package outbid.clone.ranking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "product_rankings",
        indexes = {
                @Index(name = "idx_pr_total", columnList = "totalAmount, lastBidAt"),
                @Index(name = "idx_pr_category_total", columnList = "categoryId, totalAmount, lastBidAt")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRanking {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private UUID categoryId;

    /** Total paid in minor units (paise). */
    @Builder.Default
    @Column(nullable = false)
    private Long totalAmount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Integer bidCount = 0;

    @Column(nullable = false)
    private LocalDateTime firstBidAt;

    @Column(nullable = false)
    private LocalDateTime lastBidAt;

    @Version
    private Long version;
}
