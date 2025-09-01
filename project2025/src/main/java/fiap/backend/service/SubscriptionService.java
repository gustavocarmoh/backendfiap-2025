package fiap.backend.service;

import fiap.backend.domain.Subscription;
import fiap.backend.dto.SubscriptionRequest;
import fiap.backend.dto.SubscriptionResponse;
import fiap.backend.dto.SubscriptionStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(UUID userId, SubscriptionRequest request);

    SubscriptionResponse getSubscriptionById(UUID id);

    List<SubscriptionResponse> getSubscriptionsByUserId(UUID userId);

    List<SubscriptionResponse> getAllSubscriptions();

    List<SubscriptionResponse> getPendingSubscriptions();

    List<SubscriptionResponse> getSubscriptionsByStatus(Subscription.SubscriptionStatus status);

    SubscriptionResponse updateSubscriptionStatus(UUID id, UUID adminUserId, SubscriptionStatusUpdateRequest request);

    SubscriptionResponse approveSubscription(UUID id, UUID adminUserId);

    SubscriptionResponse rejectSubscription(UUID id, UUID adminUserId);

    SubscriptionResponse cancelSubscription(UUID id, UUID userId);

    long countActiveSubscriptionsByUserId(UUID userId);
}
