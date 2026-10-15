package outbid.clone.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.payment.dto.SubmissionSummary;

import java.util.Optional;
import java.util.UUID;

@Component
public class SubmissionClient {

    private static final String SERVICE = "Submission service";

    private final RestClient restClient;

    public SubmissionClient(RestClient.Builder restClientBuilder, @Value("${services.submission.url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Optional<SubmissionSummary> getSubmission(UUID submissionId) {
        try {
            return Optional.ofNullable(
                    restClient.get()
                            .uri("/api/submissions/{id}", submissionId)
                            .retrieve()
                            .body(SubmissionSummary.class)
            );
        } catch (RestClientException e) {
            if (ClientErrors.isNotFound(e)) {
                return Optional.empty();
            }
            throw ClientErrors.translate(SERVICE, e);
        }
    }
}