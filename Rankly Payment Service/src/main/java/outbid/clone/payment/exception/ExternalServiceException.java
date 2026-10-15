package outbid.clone.payment.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {

    private final String service;
    private final int upstreamStatus;

    public ExternalServiceException(String service, int upstreamStatus, String message) {
        super(message);
        this.service = service;
        this.upstreamStatus = upstreamStatus;
    }

    public ExternalServiceException(String service, int upstreamStatus, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.upstreamStatus = upstreamStatus;
    }
}
