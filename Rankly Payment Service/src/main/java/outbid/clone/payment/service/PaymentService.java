package outbid.clone.payment.service;

import outbid.clone.payment.dto.PaymentOrderResponse;
import outbid.clone.payment.dto.PaymentRequest;
import outbid.clone.payment.dto.PaymentResponse;
import outbid.clone.payment.dto.PaymentVerifyRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(UUID id);

    List<PaymentResponse> getPaymentsBySubmission(UUID submissionId);

    PaymentOrderResponse createPaymentOrder(PaymentRequest request);

    PaymentResponse verifyPayment(PaymentVerifyRequest request);
}