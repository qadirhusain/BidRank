package outbid.clone.payment.service;

public interface PaymentWebhookService {

    void processWebhook(String payload, String webhookSignature);
}