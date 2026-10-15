package outbid.clone.ranking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RankingResponse {

    private UUID productId;
    private UUID categoryId;
    private Integer rank;
    private Integer categoryRank;
    private Long totalAmount;
    private Long todayAmount;
    private Integer bidCount;
    private LocalDateTime lastBidAt;
}