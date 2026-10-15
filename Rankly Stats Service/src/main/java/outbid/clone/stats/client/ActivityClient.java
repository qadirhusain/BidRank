package outbid.clone.stats.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import outbid.clone.stats.dto.ActivityTotalsResponse;
import outbid.clone.stats.exception.DownstreamServiceException;

@Component
public class ActivityClient {

    private static final String SERVICE = "Activity";

    private final RestClient restClient;

    public ActivityClient(@Qualifier("activityRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ActivityTotalsResponse getTotals() {

        try {
            return restClient.get()
                    .uri("/api/activity/totals")
                    .retrieve()
                    .body(ActivityTotalsResponse.class);
        } catch (RestClientException e) {
            throw new DownstreamServiceException(SERVICE, e);
        }
    }
}