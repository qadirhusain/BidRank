package outbid.clone.stats.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.stats.exception.DownstreamServiceException;

@Component
public class ProductClient {

    private static final String SERVICE = "Product";

    private final RestClient restClient;

    public ProductClient(@Qualifier("productRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Long getProductCount() {

        try {
            return restClient.get()
                    .uri("/api/products/count")
                    .retrieve()
                    .body(Long.class);
        } catch (RestClientException e) {
            throw new DownstreamServiceException(SERVICE, e);
        }
    }
}