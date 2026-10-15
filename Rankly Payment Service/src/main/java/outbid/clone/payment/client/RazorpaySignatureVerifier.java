package outbid.clone.payment.client;

import outbid.clone.payment.config.RazorpayConfig;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class RazorpaySignatureVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final RazorpayConfig razorpayConfig;

    public RazorpaySignatureVerifier(RazorpayConfig razorpayConfig) {
        this.razorpayConfig = razorpayConfig;
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {

        if (orderId == null || paymentId == null) {
            return false;
        }

        return matches(razorpayConfig.getKeySecret(), orderId + "|" + paymentId, signature);
    }

    public boolean verifyWebhookSignature(String rawPayload, String signature) {

        if (rawPayload == null) {
            return false;
        }

        return matches(razorpayConfig.getWebhookSecret(), rawPayload, signature);
    }

    private boolean matches(String secret, String data, String providedSignature) {

        if (providedSignature == null || providedSignature.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            ));

            String expected = HexFormat.of()
                    .formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    providedSignature.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}