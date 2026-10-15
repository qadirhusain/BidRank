package outbid.clone.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import outbid.clone.payment.client.RazorpaySignatureVerifier;
import outbid.clone.payment.exception.InvalidWebhookSignatureException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import outbid.clone.payment.entity.Payment;
import outbid.clone.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

/**
 * Handles Razorpay webhooks.
 * <p>
 * Responding 2xx tells Razorpay the event is done; any error response makes it
 * retry. So events we cannot or should not act on (unknown orders, other event
 * types) return normally, while a failure to credit the bid is allowed to
 * propagate so that Razorpay delivers the event again.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final RazorpaySignatureVerifier signatureVerifier;
    private final PaymentRepository paymentRepository;
    private final PaymentSettlementService settlementService;
    private final ObjectMapper objectMapper;

    @Override
    public void processWebhook(String payload, String webhookSignature) {

        if (!signatureVerifier.verifyWebhookSignature(payload, webhookSignature)) {
            throw new InvalidWebhookSignatureException("Invalid Razorpay webhook signature");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Malformed webhook payload", e);
        }

        String event = root.path("event").asString();

        switch (event) {
            case "payment.captured" -> handlePaymentCaptured(root);
            case "payment.failed" -> handlePaymentFailed(root);
            default -> log.debug("Ignoring Razorpay event: {}", event);
        }
    }

    private void handlePaymentCaptured(JsonNode root) {

        JsonNode entity = paymentEntity(root);

        String orderId = entity.path("order_id").asString(null);
        String razorpayPaymentId = entity.path("id").asString(null);

        if (!isOurOrder(orderId, razorpayPaymentId)) {
            return;
        }

        JsonNode amountNode = entity.path("amount");
        Long paidAmount = amountNode.isNumber() ? amountNode.asLong() : null;

        Payment payment = settlementService.markCaptured(
                orderId,
                razorpayPaymentId,
                null,
                entity.path("method").asString(null),
                paidAmount,
                entity.path("currency").asString(null)
        );

        // If Ranking is down this throws, Razorpay gets an error and retries the event
        settlementService.creditBid(payment.getId());
    }

    private void handlePaymentFailed(JsonNode root) {
        JsonNode entity = paymentEntity(root);

        String orderId = entity.path("order_id").asString(null);
        String razorpayPaymentId = entity.path("id").asString(null);

        if (!isOurOrder(orderId, razorpayPaymentId)) {
            return;
        }

        settlementService.markFailed(
                orderId,
                entity.path("error_description").asString(null)
        );
    }

    private JsonNode paymentEntity(JsonNode root) {
        return root.path("payload").path("payment").path("entity");
    }

    /** The Razorpay account may also take payments that are not ours. Skip those quietly. */
    private boolean isOurOrder(String orderId, String razorpayPaymentId) {
        if (orderId == null || orderId.isBlank()) {
            log.warn("Webhook for payment {} has no order id, ignoring", razorpayPaymentId);
            return false;
        }

        if (paymentRepository.findByRazorpayOrderId(orderId).isEmpty()) {
            log.warn("Webhook for payment {} refers to unknown order {}, ignoring",
                    razorpayPaymentId, orderId);
            return false;
        }

        return true;
    }
}