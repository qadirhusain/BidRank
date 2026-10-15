package outbid.clone.stats.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.stats.exception.DownstreamServiceException;

@Component
public class CategoryClient {

    private static final String SERVICE = "Category";

    private final RestClient restClient;

    public CategoryClient(@Qualifier("categoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Long getCategoryCount() {

        try {
            return restClient.get()
                    .uri("/api/categories/count")
                    .retrieve()
                    .body(Long.class);
        } catch (RestClientException e) {
            throw new DownstreamServiceException(SERVICE, e);
        }
    }
}