package outbid.clone.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSummary {

    private UUID id;
    private String title;
    private UUID categoryId;
    private String status;
}
