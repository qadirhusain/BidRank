package outbid.clone.activity.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatsResponse {

    private UUID productId;
    private Long clickCount;
    private Long viewCount;
    private LocalDateTime lastClickedAt;
}