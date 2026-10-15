package outbid.clone.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(@Value("${services.product.base-url}") String baseUrl) {
        return build(baseUrl);
    }

    @Bean
    public RestClient categoryRestClient(@Value("${services.category.base-url}") String baseUrl) {
        return build(baseUrl);
    }

    @Bean
    public RestClient activityRestClient(@Value("${services.activity.base-url}") String baseUrl) {
        return build(baseUrl);
    }

    private static RestClient build(String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}