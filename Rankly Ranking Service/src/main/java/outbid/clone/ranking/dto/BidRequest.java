package outbid.clone.ranking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BidRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Long amount;

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;
}