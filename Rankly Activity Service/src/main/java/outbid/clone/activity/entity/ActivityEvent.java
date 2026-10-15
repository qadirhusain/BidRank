package outbid.clone.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "activity_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_activity_visitor",
                columnNames = {"productId", "activityType", "visitorHash", "eventDate"}),
        indexes = {
                @Index(name = "idx_activity_product_date", columnList = "productId, eventDate"),
                @Index(name = "idx_activity_date", columnList = "eventDate")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEvent {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(nullable = false)
        private UUID productId;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 10)
        private ActivityType activityType;

        @Column(nullable = false, length = 64)
        private String visitorHash;

        @Column(nullable = false)
        private LocalDate eventDate;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;
}
