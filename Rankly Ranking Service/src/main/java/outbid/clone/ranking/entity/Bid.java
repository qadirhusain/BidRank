package outbid.clone.ranking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "bids",
        uniqueConstraints = @UniqueConstraint(name = "uk_bid_payment", columnNames = "paymentId"),
        indexes = {
                @Index(name = "idx_bid_product", columnList = "productId"),
                @Index(name = "idx_bid_date", columnList = "bidDate")
        }
)
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID categoryId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private LocalDate bidDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}