package outbid.clone.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import outbid.clone.payment.client.*;
import outbid.clone.payment.config.RazorpayConfig;
import outbid.clone.payment.dto.*;
import outbid.clone.payment.entity.Payment;
import outbid.clone.payment.entity.PaymentStatus;
import outbid.clone.payment.exception.*;
import outbid.clone.payment.repository.PaymentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final long PAISE_PER_RUPEE = 100L;
    private static final long MIN_PAYMENT_PAISE = 10 * PAISE_PER_RUPEE;

    private final PaymentRepository paymentRepository;
    private final PaymentSettlementService settlementService;
    private final RazorpayClient razorpayClient;
    private final RazorpaySignatureVerifier signatureVerifier;
    private final RazorpayConfig razorpayConfig;
    private final SubmissionClient submissionClient;
    private final ProductClient productClient;
    private final RankingClient rankingClient;
    private final ModelMapper modelMapper;

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository
                .findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + id));

        return toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsBySubmission(UUID submissionId) {
        return paymentRepository
                .findBySubmissionIdOrderByCreatedAtDesc(submissionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /*
     * Deliberately not @Transactional: each save is its own short transaction, so
     * no database transaction is held open during the call to Razorpay.
     */
    @Override
    public PaymentOrderResponse createPaymentOrder(PaymentRequest request) {

        // 1. The submission must exist and be approved
        SubmissionSummary submission = submissionClient
                .getSubmission(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission not found with ID: " + request.getSubmissionId()));

        if (!"APPROVED".equalsIgnoreCase(submission.getStatus())) {
            throw new InvalidBidException("Only approved submissions can place a bid");
        }

        // 2. Find the product the bid will be credited to
        ProductSummary product = resolveProduct(submission);

        if (product.getStatus() != null
                && !"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new InvalidBidException("This product is not accepting bids");
        }

        // 3. The listing's current total, in paise. A product with no bids yet has none.
        long previousTotal = rankingClient
                .getProductStanding(product.getId())
                .map(RankingStanding::getTotalAmount)
                .orElse(0L);

        // 4. The user pays only the difference between the target and the current total
        long targetBid = toPaise(request.getTargetBid());

        if (targetBid <= previousTotal) {
            throw new InvalidBidException(
                    "Target bid must be greater than the current total of ₹"
                            + formatRupees(previousTotal));
        }

        long amount = targetBid - previousTotal;
        if (amount < MIN_PAYMENT_PAISE){
            throw new InvalidBidException("Minimum payment is ₹" + (MIN_PAYMENT_PAISE / PAISE_PER_RUPEE));
        }

        // 5. Record the payment before calling Razorpay, so every attempt is traceable
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CREATED);
        payment.setSubmissionId(submission.getId() != null ? submission.getId() : request.getSubmissionId());
        payment.setProductId(product.getId());
        payment.setTargetBid(targetBid);
        payment.setPreviousTotal(previousTotal);
        payment.setAmount(amount);

        Payment saved = paymentRepository.save(payment);

        // Razorpay limits the receipt to 40 characters: a UUID without dashes is 32
        String receipt = saved.getId().toString().replace("-", "");

        // 6. Create the Razorpay order
        try {
            RazorpayClient.RazorpayOrder order = razorpayClient.createOrder(
                    saved.getAmount(),
                    saved.getCurrency(),
                    receipt
            );

            saved.setRazorpayOrderId(order.id());
            saved.setStatus(PaymentStatus.PENDING);
            saved = paymentRepository.save(saved);

        } catch (RuntimeException e) {
            saved.setStatus(PaymentStatus.FAILED);
            saved.setFailureReason("Unable to create Razorpay order");
            paymentRepository.save(saved);
            throw e;
        }

        return PaymentOrderResponse.builder()
                .paymentId(saved.getId())
                .submissionId(saved.getSubmissionId())
                .productId(saved.getProductId())
                .previousTotal(saved.getPreviousTotal())
                .targetBid(saved.getTargetBid())
                .amount(saved.getAmount())
                .currency(saved.getCurrency())
                .status(saved.getStatus())
                .razorpayOrderId(saved.getRazorpayOrderId())
                .razorpayKeyId(razorpayConfig.getKeyId())
                .build();
    }

    @Override
    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for Razorpay order: " + request.getRazorpayOrderId()));

        // A bad signature is rejected without changing the payment. Anyone who
        // knows an order id must not be able to fail someone else's payment.
        boolean signatureValid = signatureVerifier.verifyPaymentSignature(
                payment.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!signatureValid) {
            throw new InvalidPaymentSignatureException("Invalid Razorpay payment signature");
        }

        Payment settled = settlementService.markCaptured(
                payment.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                null,
                null,
                null
        );

        try {
            settled = settlementService.creditBid(settled.getId());
        } catch (ExternalServiceException e) {
            // The customer has paid. Do not fail the response: bidRecordedAt stays
            // empty, and the webhook or a later retry credits the bid.
            log.error("Payment {} succeeded but the bid could not be credited yet: {}",
                    settled.getId(), e.getMessage());
        }

        return toResponse(settled);
    }

    private ProductSummary resolveProduct(SubmissionSummary submission) {
        if (submission.getProductId() == null) {
            throw new InvalidBidException("This submission has no product yet");
        }

        return productClient.getProductById(submission.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + submission.getProductId()));
    }

    private long toPaise(long rupees) {
        try {
            return Math.multiplyExact(rupees, PAISE_PER_RUPEE);
        } catch (ArithmeticException e) {
            throw new InvalidBidException("Target bid is too large");
        }
    }

    private String formatRupees(long paise) {
        return paise % PAISE_PER_RUPEE == 0
                ? String.valueOf(paise / PAISE_PER_RUPEE)
                : String.format("%.2f", paise / (double) PAISE_PER_RUPEE);
    }

    private PaymentResponse toResponse(Payment payment) {
        return modelMapper.map(payment, PaymentResponse.class);
    }
}