package outbid.clone.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Submission ID is required")
    private UUID submissionId;

    /** Total the listing should reach, in whole rupees. The user pays only the difference. */
    @NotNull(message = "Target bid is required")
    @Positive(message = "Target bid must be greater than zero")
    private Long targetBid;
}