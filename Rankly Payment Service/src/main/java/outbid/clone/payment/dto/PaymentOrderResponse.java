package outbid.clone.payment.dto;

import lombok.*;
import outbid.clone.payment.entity.PaymentStatus;

import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private UUID paymentId;
    private UUID submissionId;
    private UUID productId;
    private Long previousTotal;
    private Long targetBid;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private String razorpayOrderId;
    private String razorpayKeyId;
}
