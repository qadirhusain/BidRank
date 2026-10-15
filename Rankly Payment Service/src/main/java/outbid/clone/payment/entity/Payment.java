package outbid.clone.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_razorpay_order", columnNames = "razorpay_order_id"),
                @UniqueConstraint(name = "uk_payment_razorpay_payment", columnNames = "razorpay_payment_id")
        },
        indexes = {
                @Index(name = "idx_payment_submission", columnList = "submission_id"),
                @Index(name = "idx_payment_product", columnList = "product_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_created", columnList = "created_at")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "target_bid", nullable = false)
    private Long targetBid;

    @Column(name = "previous_total", nullable = false)
    private Long previousTotal;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 500)
    private String razorpaySignature;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "bid_recorded_at")
    private LocalDateTime bidRecordedAt;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}