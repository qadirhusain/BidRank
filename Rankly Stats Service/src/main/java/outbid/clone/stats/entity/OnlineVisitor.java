package outbid.clone.stats.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "online_visitors",
        indexes = @Index(name = "idx_online_visitor_last_seen", columnList = "last_seen")
)
public class OnlineVisitor {

    @Id
    @Column(name = "visitor_id", nullable = false, length = 100)
    private String visitorId;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;
}