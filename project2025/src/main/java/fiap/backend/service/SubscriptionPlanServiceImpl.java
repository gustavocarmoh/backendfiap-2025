package fiap.backend.service;

import fiap.backend.domain.SubscriptionPlan;
import fiap.backend.dto.SubscriptionPlanRequest;
import fiap.backend.dto.SubscriptionPlanResponse;
import fiap.backend.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanServiceImpl(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    @Override
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        if (subscriptionPlanRepository.existsByName(request.getName())) {
            throw new RuntimeException("A plan with this name already exists");
        }

        SubscriptionPlan plan = new SubscriptionPlan(request.getName(), request.getPrice());
        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);

        return mapToResponse(savedPlan);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(UUID id, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // Check if another plan with the same name exists
        if (!plan.getName().equals(request.getName()) &&
                subscriptionPlanRepository.existsByName(request.getName())) {
            throw new RuntimeException("A plan with this name already exists");
        }

        plan.setName(request.getName());
        plan.setPrice(request.getPrice());

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(plan);
        return mapToResponse(updatedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(UUID id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return mapToResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        return subscriptionPlanRepository.findAllActivePlansOrderByPrice()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        return subscriptionPlanRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivatePlan(UUID id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setIsActive(false);
        subscriptionPlanRepository.save(plan);
    }

    @Override
    public void activatePlan(UUID id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setIsActive(true);
        subscriptionPlanRepository.save(plan);
    }

    @Override
    public void deletePlan(UUID id) {
        if (!subscriptionPlanRepository.existsById(id)) {
            throw new RuntimeException("Plan not found");
        }
        subscriptionPlanRepository.deleteById(id);
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getPrice(),
                plan.getCreatedAt(),
                plan.getUpdatedAt(),
                plan.getIsActive());
    }
}
