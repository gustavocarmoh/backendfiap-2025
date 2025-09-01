package fiap.backend.dto;

import fiap.backend.domain.Subscription.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;

public class SubscriptionStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private SubscriptionStatus status;

    // Constructors
    public SubscriptionStatusUpdateRequest() {
    }

    public SubscriptionStatusUpdateRequest(SubscriptionStatus status) {
        this.status = status;
    }

    // Getters and Setters
    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }
}
