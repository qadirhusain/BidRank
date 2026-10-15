package outbid.clone.activity.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTotalsResponse {
    private Long totalClicks;
    private Long totalViews;
}
