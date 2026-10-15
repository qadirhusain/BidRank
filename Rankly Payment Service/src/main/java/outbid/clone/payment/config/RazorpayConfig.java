package outbid.clone.payment.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.base-url:https://api.razorpay.com/v1}")
    private String baseUrl;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    void validate() {
        require(keyId, "razorpay.key-id");
        require(keySecret, "razorpay.key-secret");
        require(webhookSecret, "razorpay.webhook-secret");
    }

    private static void require(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + property);
        }
    }
}