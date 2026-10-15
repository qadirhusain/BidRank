package outbid.clone.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.client.RestClientException;
import outbid.clone.payment.config.RazorpayConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import outbid.clone.payment.exception.ExternalServiceException;

import java.util.Map;

@Component
public class RazorpayClient {

    private static final String SERVICE = "Razorpay";

    private final RestClient restClient;

    public RazorpayClient(RestClient.Builder restClientBuilder, RazorpayConfig razorpayConfig) {
        this.restClient = restClientBuilder
                .baseUrl(razorpayConfig.getBaseUrl())
                .defaultHeaders(headers ->
                        headers.setBasicAuth(
                                razorpayConfig.getKeyId(),
                                razorpayConfig.getKeySecret()
                        )
                )
                .build();
    }

    public RazorpayOrder createOrder(long amountPaise, String currency, String receipt) {
        try {
            RazorpayOrder order = restClient.post()
                    .uri("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "amount", amountPaise,
                            "currency", currency,
                            "receipt", receipt
                    ))
                    .retrieve()
                    .body(RazorpayOrder.class);

            if (order == null || order.id() == null || order.id().isBlank()) {
                throw new ExternalServiceException(SERVICE, 502, "Razorpay did not return an order ID");
            }

            return order;
        } catch (RestClientException e) {
            throw ClientErrors.translate(SERVICE, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RazorpayOrder(
            String id,
            Long amount,
            String currency,
            String status,
            @JsonProperty("receipt") String receipt
    ) {

    }
}