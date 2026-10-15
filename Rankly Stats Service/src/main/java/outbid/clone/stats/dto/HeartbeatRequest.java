package outbid.clone.stats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeartbeatRequest {

    @NotBlank(message = "Visitor ID is required")
    @Size(max = 100, message = "Visitor ID must not exceed 100 characters")
    private String visitorId;
}
