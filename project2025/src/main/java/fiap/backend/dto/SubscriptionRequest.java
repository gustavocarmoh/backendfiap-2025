package fiap.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SubscriptionRequest {

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    // Constructors
    public SubscriptionRequest() {
    }

    public SubscriptionRequest(UUID planId) {
        this.planId = planId;
    }

    // Getters and Setters
    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }
}
