package outbid.clone.activity.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.activity.exception.ProductNotFoundException;
import outbid.clone.activity.exception.ProductServiceUnavailableException;

import java.util.UUID;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Qualifier("productRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void validateProduct(UUID productId) {
        try {
            restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        } catch (RestClientException e) {
            throw new ProductServiceUnavailableException(
                    "Product service is currently unavailable");
        }
    }
}