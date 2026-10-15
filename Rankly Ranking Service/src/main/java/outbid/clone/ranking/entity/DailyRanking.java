package outbid.clone.ranking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
        name = "daily_rankings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_product_date", columnNames = {"productId", "bidDate"}),
        indexes = {
                @Index(name = "idx_daily_date_amount", columnList = "bidDate, amount, lastBidAt"),
                @Index(name = "idx_daily_category_date", columnList = "categoryId, bidDate, amount")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID categoryId;

    @Column(nullable = false)
    private LocalDate bidDate;

    @Builder.Default
    @Column(nullable = false)
    private Long amount = 0L;

    @Column(nullable = false)
    private LocalDateTime lastBidAt;

    @Version
    private Long version;
}
