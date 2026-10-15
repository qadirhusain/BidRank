package outbid.clone.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankingStanding {

    private UUID productId;
    private Integer rank;
    private Integer categoryRank;
    private Long totalAmount;
    private Long todayAmount;
    private Integer bidCount;
}
