package outbid.clone.ranking.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.ranking.dto.ProductSummary;
import outbid.clone.ranking.exception.ProductNotFoundException;
import outbid.clone.ranking.exception.ProductServiceUnavailableException;

import java.util.UUID;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Qualifier("productRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductSummary getProduct(UUID productId) {

        try {
            ProductSummary product = restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductSummary.class);

            if (product == null) {
                throw new ProductNotFoundException("Product not found with id: " + productId);
            }
            return product;

        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        } catch (RestClientException e) {
            throw new ProductServiceUnavailableException(
                    "Product service is currently unavailable");
        }
    }
}