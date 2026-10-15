package outbid.clone.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionSummary {

    private UUID id;
    private String title;
    private UUID productId;
    private String url;
    private String status;
}