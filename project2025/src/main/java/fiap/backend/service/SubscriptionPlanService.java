package fiap.backend.service;

import fiap.backend.dto.SubscriptionPlanRequest;
import fiap.backend.dto.SubscriptionPlanResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanService {

    SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request);

    SubscriptionPlanResponse updatePlan(UUID id, SubscriptionPlanRequest request);

    SubscriptionPlanResponse getPlanById(UUID id);

    List<SubscriptionPlanResponse> getAllActivePlans();

    List<SubscriptionPlanResponse> getAllPlans();

    void deactivatePlan(UUID id);

    void activatePlan(UUID id);

    void deletePlan(UUID id);
}
