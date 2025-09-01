package fiap.backend.service;

import fiap.backend.domain.Subscription;
import fiap.backend.domain.SubscriptionPlan;
import fiap.backend.domain.User;
import fiap.backend.dto.SubscriptionRequest;
import fiap.backend.dto.SubscriptionResponse;
import fiap.backend.dto.SubscriptionStatusUpdateRequest;
import fiap.backend.repository.SubscriptionPlanRepository;
import fiap.backend.repository.SubscriptionRepository;
import fiap.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SubscriptionResponse createSubscription(UUID userId, SubscriptionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (!plan.getIsActive()) {
            throw new RuntimeException("Plan is not active");
        }

        Subscription subscription = new Subscription(user, plan);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return mapToResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdWithUserAndPlan(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        return mapToResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAllWithUserAndPlan()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getPendingSubscriptions() {
        return subscriptionRepository.findByStatusWithUserAndPlan(Subscription.SubscriptionStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByStatus(Subscription.SubscriptionStatus status) {
        return subscriptionRepository.findByStatusWithUserAndPlan(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionResponse updateSubscriptionStatus(UUID id, UUID adminUserId,
            SubscriptionStatusUpdateRequest request) {
        Subscription subscription = subscriptionRepository.findByIdWithUserAndPlan(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        switch (request.getStatus()) {
            case APPROVED:
                // Antes de aprovar a nova assinatura, cancela todas as assinaturas ativas do
                // usuário
                cancelActiveSubscriptionsForUser(subscription.getUser().getId());
                subscription.approve(adminUserId);
                break;
            case REJECTED:
                subscription.reject(adminUserId);
                break;
            case CANCELLED:
                subscription.cancel();
                break;
            default:
                throw new RuntimeException("Invalid status update");
        }

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse approveSubscription(UUID id, UUID adminUserId) {
        Subscription subscription = subscriptionRepository.findByIdWithUserAndPlan(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (!subscription.isPending()) {
            throw new RuntimeException("Only pending subscriptions can be approved");
        }

        // Antes de aprovar a nova assinatura, cancela todas as assinaturas ativas do
        // usuário
        cancelActiveSubscriptionsForUser(subscription.getUser().getId());

        subscription.approve(adminUserId);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(savedSubscription);
    }

    @Override
    public SubscriptionResponse rejectSubscription(UUID id, UUID adminUserId) {
        Subscription subscription = subscriptionRepository.findByIdWithUserAndPlan(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (!subscription.isPending()) {
            throw new RuntimeException("Only pending subscriptions can be rejected");
        }

        subscription.reject(adminUserId);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(savedSubscription);
    }

    @Override
    public SubscriptionResponse cancelSubscription(UUID id, UUID userId) {
        Subscription subscription = subscriptionRepository.findByIdWithUserAndPlan(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Check if the user owns this subscription
        if (!subscription.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own subscriptions");
        }

        if (!subscription.isApproved()) {
            throw new RuntimeException("Only approved subscriptions can be cancelled");
        }

        subscription.cancel();
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.countActiveSubscriptionsByUserId(userId);
    }

    /**
     * Cancela todas as assinaturas ativas de um usuário
     * Implementa a regra de negócio: um usuário pode ter apenas 1 plano ativo por
     * vez
     */
    private void cancelActiveSubscriptionsForUser(UUID userId) {
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptionsByUserId(userId);

        for (Subscription activeSubscription : activeSubscriptions) {
            activeSubscription.cancel();
            subscriptionRepository.save(activeSubscription);
        }
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setUserId(subscription.getUser().getId());
        response.setUserEmail(subscription.getUser().getEmail());
        response.setUserName(subscription.getUser().getFirstName() + " " + subscription.getUser().getLastName());
        response.setPlanId(subscription.getPlan().getId());
        response.setPlanName(subscription.getPlan().getName());
        response.setAmount(subscription.getAmount());
        response.setStatus(subscription.getStatus());
        response.setApprovedByUserId(subscription.getApprovedByUserId());
        response.setSubscriptionDate(subscription.getSubscriptionDate());
        response.setApprovedDate(subscription.getApprovedDate());
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());

        // Set approved by user email if available
        if (subscription.getApprovedByUserId() != null) {
            userRepository.findById(subscription.getApprovedByUserId())
                    .ifPresent(approver -> response.setApprovedByUserEmail(approver.getEmail()));
        }

        return response;
    }
}
