package outbid.clone.activity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "product_activity_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductActivityStats {

    @Id
    private UUID productId;

    @Builder.Default
    @Column(nullable = false)
    private Long clickCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    private LocalDateTime lastClickedAt;

    @Version
    private Long version;
}
