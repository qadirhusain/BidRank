package outbid.clone.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.payment.dto.ProductSummary;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProductClient {

    private static final String SERVICE = "Product service";

    private final RestClient restClient;

    public ProductClient(RestClient.Builder restClientBuilder, @Value("${services.product.url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Optional<ProductSummary> getProductById(UUID productId) {

        try {
            return Optional.ofNullable(
                    restClient.get()
                            .uri("/api/products/{id}", productId)
                            .retrieve()
                            .body(ProductSummary.class)
            );
        } catch (RestClientException e) {
            if (ClientErrors.isNotFound(e)) {
                return Optional.empty();
            }
            throw ClientErrors.translate(SERVICE, e);
        }
    }
}
