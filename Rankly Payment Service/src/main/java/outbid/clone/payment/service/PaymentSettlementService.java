package outbid.clone.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import outbid.clone.payment.client.RankingClient;
import outbid.clone.payment.entity.Payment;
import outbid.clone.payment.entity.PaymentStatus;
import outbid.clone.payment.exception.PaymentNotFoundException;
import outbid.clone.payment.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class PaymentSettlementService {

    private final PaymentRepository paymentRepository;
    private final RankingClient rankingClient;
    private final TransactionTemplate transactionTemplate;

    public PaymentSettlementService(
            PaymentRepository paymentRepository,
            RankingClient rankingClient,
            PlatformTransactionManager transactionManager
    ) {
        this.paymentRepository = paymentRepository;
        this.rankingClient = rankingClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Marks the payment for this Razorpay order as SUCCESS. Safe to call repeatedly.
     *
     * @param signature    Checkout signature, or null when called from the webhook
     * @param method       payment method, or null when called from verify
     * @param paidAmount   amount Razorpay says was captured, in paise, or null when unknown
     * @param paidCurrency currency Razorpay says was captured, or null when unknown
     */
    public Payment markCaptured(
            String razorpayOrderId,
            String razorpayPaymentId,
            String signature,
            String method,
            Long paidAmount,
            String paidCurrency
    ) {
        return transactionTemplate.execute(status -> {
            Payment payment = paymentRepository
                    .findByRazorpayOrderIdForUpdate(razorpayOrderId)
                    .orElseThrow(() -> new PaymentNotFoundException(
                            "Payment not found for Razorpay order: " + razorpayOrderId));

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                if (payment.getPaymentMethod() == null && method != null) {
                    payment.setPaymentMethod(method);
                    return paymentRepository.save(payment);
                }
                return payment;
            }

            if (payment.getStatus() == PaymentStatus.REFUNDED) {
                return payment;
            }

            boolean amountMismatch = paidAmount != null
                    && !paidAmount.equals(payment.getAmount());
            boolean currencyMismatch = paidCurrency != null
                    && !paidCurrency.equalsIgnoreCase(payment.getCurrency());

            if (amountMismatch || currencyMismatch) {
                log.error("Captured payment {} does not match order {}: expected {} {}, got {} {}",
                        razorpayPaymentId, razorpayOrderId,
                        payment.getAmount(), payment.getCurrency(),
                        paidAmount, paidCurrency);

                payment.setFailureReason("Captured amount or currency does not match the order. Needs manual review.");
                return paymentRepository.save(payment);
            }

            payment.setRazorpayPaymentId(razorpayPaymentId);

            if (signature != null) {
                payment.setRazorpaySignature(signature);
            }
            if (method != null) {
                payment.setPaymentMethod(method);
            }

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setFailureReason(null);

            return paymentRepository.save(payment);
        });
    }

    /**
     * Records a failed attempt. A failed attempt does not close the order, because
     * the customer can retry in Checkout, so a later success still wins. A payment
     * that has already succeeded is never touched.
     */
    public void markFailed(String razorpayOrderId, String reason) {
        transactionTemplate.executeWithoutResult(status ->
                paymentRepository.findByRazorpayOrderIdForUpdate(razorpayOrderId)
                        .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                        .ifPresent(payment -> {
                            payment.setStatus(PaymentStatus.FAILED);
                            payment.setFailureReason(reason);
                            paymentRepository.save(payment);
                        })
        );
    }

    /**
     * Credits the paid amount to the product in the Ranking service, once.
     * Does nothing unless the payment is SUCCESS and not yet credited.
     * Ranking is idempotent on the payment id, so retrying after a failure is safe.
     *
     * @throws outbid.clone.payment.exception.ExternalServiceException if Ranking is unavailable.
     *         The payment stays SUCCESS with bidRecordedAt empty, and the next call retries.
     */
    public Payment creditBid(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS || payment.getBidRecordedAt() != null) {
            return payment;
        }

        rankingClient.recordBid(
                payment.getProductId(),
                payment.getAmount(),
                payment.getId()
        );

        try {
            return transactionTemplate.execute(status -> {
                Payment fresh = paymentRepository.findById(paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId));

                if (fresh.getBidRecordedAt() == null) {
                    fresh.setBidRecordedAt(LocalDateTime.now());
                    return paymentRepository.save(fresh);
                }
                return fresh;
            });
        } catch (ObjectOptimisticLockingFailureException e) {
            // Another request recorded it at the same moment
            return paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException(
                            "Payment not found with id: " + paymentId));
        }
    }
}
