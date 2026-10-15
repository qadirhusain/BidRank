package outbid.clone.payment.controller;

import lombok.RequiredArgsConstructor;
import outbid.clone.payment.exception.InvalidWebhookSignatureException;
import outbid.clone.payment.service.PaymentWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    /**
     * Called by Razorpay, not by the frontend.
     * <p>
     * The body must be taken as a raw String: the signature is calculated over the
     * exact bytes Razorpay sent, so it must not be parsed and re-serialized first.
     * A 2xx response tells Razorpay the event is handled. Any error response makes
     * Razorpay retry later.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String webhookSignature,
            @RequestBody String payload
    ) {

        if (webhookSignature == null || webhookSignature.isBlank()) {
            throw new InvalidWebhookSignatureException("Missing webhook signature");
        }

        paymentWebhookService.processWebhook(payload, webhookSignature);

        return ResponseEntity.ok().build();
    }
}