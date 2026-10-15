package outbid.clone.ranking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ProductSummary {

    private UUID id;
    private UUID categoryId;
    private String title;
}
