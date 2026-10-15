package outbid.clone.submission.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient categoryRestClient(
            @Value("${services.category.base-url}") String baseUrl
    ) {
        return build(baseUrl, Duration.ofSeconds(3));
    }

    @Bean
    public RestClient productRestClient(
            @Value("${services.product.base-url}") String baseUrl
    ) {
        return build(baseUrl, Duration.ofSeconds(5));
    }

    private static RestClient build(String baseUrl, Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}