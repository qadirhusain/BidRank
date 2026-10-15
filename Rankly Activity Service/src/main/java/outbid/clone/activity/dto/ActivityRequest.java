package outbid.clone.activity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ActivityRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;
}