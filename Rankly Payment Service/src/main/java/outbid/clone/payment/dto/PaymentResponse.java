package outbid.clone.payment.dto;

import lombok.Getter;
import lombok.Setter;
import outbid.clone.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponse {
    private UUID id;
    private UUID submissionId;
    private UUID productId;
    private Long targetBid;
    private Long previousTotal;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String paymentMethod;
    private String failureReason;
    private LocalDateTime bidRecordedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}