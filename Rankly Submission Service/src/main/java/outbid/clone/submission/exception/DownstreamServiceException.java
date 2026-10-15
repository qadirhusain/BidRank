package outbid.clone.submission.exception;

import lombok.Getter;

@Getter
public class DownstreamServiceException extends RuntimeException {

    private final String service;

    public DownstreamServiceException(String service, Throwable cause) {
        super(service + " service is currently unavailable", cause);
        this.service = service;
    }

}
