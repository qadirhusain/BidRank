package outbid.clone.submission.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.submission.exception.CategoryNotFoundException;
import outbid.clone.submission.exception.DownstreamServiceException;

import java.util.UUID;

@Component
public class CategoryClient {

    private final RestClient restClient;

    public CategoryClient(@Qualifier("categoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void validateCategory(UUID categoryId) {

        try {
            restClient.get()
                    .uri("/api/categories/{id}", categoryId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound e) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        } catch (RestClientException e) {
            throw new DownstreamServiceException("Category", e);
        }
    }
}
