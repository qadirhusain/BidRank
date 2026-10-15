package outbid.clone.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.payment.dto.RankingStanding;
import outbid.clone.payment.exception.ExternalServiceException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class RankingClient {

    private static final String SERVICE = "Ranking service";

    private final RestClient restClient;

    public RankingClient(RestClient.Builder restClientBuilder, @Value("${services.ranking.url}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Optional<RankingStanding> getProductStanding(UUID productId) {
        try {
            return Optional.ofNullable(
                    restClient.get()
                            .uri("/api/rankings/products/{productId}", productId)
                            .retrieve()
                            .body(RankingStanding.class)
            );
        } catch (RestClientException e) {
            if (ClientErrors.isNotFound(e)) {
                return Optional.empty();
            }
            throw ClientErrors.translate(SERVICE, e);
        }
    }

    public RankingStanding recordBid(UUID productId, long amountPaise, UUID paymentId) {
        try {
            RankingStanding standing = restClient.post()
                    .uri("/api/rankings/bids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "productId", productId,
                            "amount", amountPaise,
                            "paymentId", paymentId
                    ))
                    .retrieve()
                    .body(RankingStanding.class);

            if (standing == null) {
                throw new ExternalServiceException(SERVICE, 502, SERVICE + " returned an empty response");
            }

            return standing;

        } catch (RestClientException e) {
            throw ClientErrors.translate(SERVICE, e);
        }
    }
}
