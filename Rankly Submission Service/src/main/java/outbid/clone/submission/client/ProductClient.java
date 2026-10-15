package outbid.clone.submission.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.submission.dto.ProductCreateRequest;
import outbid.clone.submission.dto.ProductCreateResponse;
import outbid.clone.submission.exception.DownstreamServiceException;
import outbid.clone.submission.exception.InvalidSubmissionRequestException;
import outbid.clone.submission.exception.SubmissionConflictException;

import java.util.UUID;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Qualifier("productRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public UUID createProduct(ProductCreateRequest request) {
        try {
            ProductCreateResponse created = restClient.post()
                    .uri("/api/products")
                    .body(request)
                    .retrieve()
                    .body(ProductCreateResponse.class);

            if (created == null || created.id() == null) {
                throw new DownstreamServiceException("Product",
                        new IllegalStateException("Product service returned no id"));
            }
            return created.id();

        } catch (HttpClientErrorException.Conflict e) {
            throw new SubmissionConflictException(
                    "A product with url '" + request.getUrl() + "' already exists");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new InvalidSubmissionRequestException(
                    "Product service rejected the submission: " + e.getResponseBodyAsString());
        } catch (HttpClientErrorException e) {
            // Any other 4xx (404, 422...) is a problem with the request, not a Product service outage
            throw new InvalidSubmissionRequestException(
                    "Product service rejected the submission (" + e.getStatusCode().value() + "): "
                            + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new DownstreamServiceException("Product", e);
        }
    }
}