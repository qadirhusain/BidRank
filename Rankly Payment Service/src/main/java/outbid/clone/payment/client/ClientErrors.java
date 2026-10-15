package outbid.clone.payment.client;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import outbid.clone.payment.exception.ExternalServiceException;

final class ClientErrors {

    private static final int MAX_BODY_LENGTH = 300;

    private ClientErrors() {
    }

    static boolean isNotFound(RestClientException e) {
        return e instanceof RestClientResponseException response
                && response.getStatusCode().value() == 404;
    }

    static ExternalServiceException translate(String service, RestClientException e) {
        if (e instanceof RestClientResponseException response) {
            int status = response.getStatusCode().value();
            return new ExternalServiceException(
                    service,
                    status,
                    service + " responded with " + status + ": " + truncate(response.getResponseBodyAsString()),
                    e
            );
        }

        return new ExternalServiceException(service, 503, service + " is unreachable", e);
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= MAX_BODY_LENGTH ? body : body.substring(0, MAX_BODY_LENGTH) + "...";
    }

}