package outbid.clone.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AmountToBeatResponse {

    private Long leaderAmount;
    private Long amountToBeat;
}
