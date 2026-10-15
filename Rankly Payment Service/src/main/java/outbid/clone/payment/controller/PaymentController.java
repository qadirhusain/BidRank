package outbid.clone.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import outbid.clone.payment.dto.PaymentOrderResponse;
import outbid.clone.payment.dto.PaymentRequest;
import outbid.clone.payment.dto.PaymentResponse;
import outbid.clone.payment.dto.PaymentVerifyRequest;
import outbid.clone.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsBySubmission(
            @PathVariable UUID submissionId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentsBySubmission(submissionId));
    }

    /**
     * Step 1 of paying: creates the Razorpay order for the difference between the
     * requested total and the listing's current total. The response carries what
     * the frontend needs to open Razorpay Checkout.
     */
    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.createPaymentOrder(request));
    }

    /**
     * Step 2 of paying: the frontend sends the three values Razorpay Checkout
     * returns. On success the payment is marked SUCCESS and the bid is credited.
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request
    ) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }
}