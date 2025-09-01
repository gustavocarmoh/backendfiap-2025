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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

        @Mock
        private SubscriptionRepository subscriptionRepository;

        @Mock
        private SubscriptionPlanRepository subscriptionPlanRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private SubscriptionServiceImpl subscriptionService;

        private User testUser;
        private SubscriptionPlan testPlan;
        private Subscription testSubscription;
        private UUID userId;
        private UUID planId;
        private UUID subscriptionId;
        private UUID adminUserId;

        @BeforeEach
        void setUp() {
                userId = UUID.randomUUID();
                planId = UUID.randomUUID();
                subscriptionId = UUID.randomUUID();
                adminUserId = UUID.randomUUID();

                testUser = new User();
                testUser.setId(userId);
                testUser.setEmail("user@test.com");
                testUser.setFirstName("John");
                testUser.setLastName("Doe");

                testPlan = new SubscriptionPlan("Test Plan", BigDecimal.valueOf(19.99));
                testPlan.setId(planId);
                testPlan.setIsActive(true);

                testSubscription = new Subscription(testUser, testPlan);
                testSubscription.setId(subscriptionId);
                testSubscription.setCreatedAt(LocalDateTime.now());
                testSubscription.setUpdatedAt(LocalDateTime.now());
        }

        @Test
        void createSubscription_Success() {
                // Given
                SubscriptionRequest request = new SubscriptionRequest(planId);
                when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
                when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(testPlan));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.createSubscription(userId, request);

                // Then
                assertNotNull(response);
                assertEquals(subscriptionId, response.getId());
                assertEquals(userId, response.getUserId());
                assertEquals(planId, response.getPlanId());
                assertEquals(Subscription.SubscriptionStatus.PENDING, response.getStatus());
                verify(userRepository).findById(userId);
                verify(subscriptionPlanRepository).findById(planId);
                verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        void createSubscription_UserNotFound() {
                // Given
                SubscriptionRequest request = new SubscriptionRequest(planId);
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.createSubscription(userId, request));
                assertEquals("User not found", exception.getMessage());
                verify(userRepository).findById(userId);
                verify(subscriptionPlanRepository, never()).findById(any());
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void createSubscription_PlanNotFound() {
                // Given
                SubscriptionRequest request = new SubscriptionRequest(planId);
                when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
                when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.createSubscription(userId, request));
                assertEquals("Plan not found", exception.getMessage());
                verify(userRepository).findById(userId);
                verify(subscriptionPlanRepository).findById(planId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void createSubscription_PlanNotActive() {
                // Given
                testPlan.setIsActive(false);
                SubscriptionRequest request = new SubscriptionRequest(planId);
                when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
                when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(testPlan));

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.createSubscription(userId, request));
                assertEquals("Plan is not active", exception.getMessage());
                verify(userRepository).findById(userId);
                verify(subscriptionPlanRepository).findById(planId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void getSubscriptionById_Success() {
                // Given
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));

                // When
                SubscriptionResponse response = subscriptionService.getSubscriptionById(subscriptionId);

                // Then
                assertNotNull(response);
                assertEquals(subscriptionId, response.getId());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
        }

        @Test
        void getSubscriptionById_NotFound() {
                // Given
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.getSubscriptionById(subscriptionId));
                assertEquals("Subscription not found", exception.getMessage());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
        }

        @Test
        void getSubscriptionsByUserId_Success() {
                // Given
                List<Subscription> subscriptions = Arrays.asList(testSubscription);
                when(subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                                .thenReturn(subscriptions);

                // When
                List<SubscriptionResponse> responses = subscriptionService.getSubscriptionsByUserId(userId);

                // Then
                assertNotNull(responses);
                assertEquals(1, responses.size());
                assertEquals(subscriptionId, responses.get(0).getId());
                verify(subscriptionRepository).findByUserIdOrderByCreatedAtDesc(userId);
        }

        @Test
        void getAllSubscriptions_Success() {
                // Given
                List<Subscription> subscriptions = Arrays.asList(testSubscription);
                when(subscriptionRepository.findAllWithUserAndPlan()).thenReturn(subscriptions);

                // When
                List<SubscriptionResponse> responses = subscriptionService.getAllSubscriptions();

                // Then
                assertNotNull(responses);
                assertEquals(1, responses.size());
                verify(subscriptionRepository).findAllWithUserAndPlan();
        }

        @Test
        void getPendingSubscriptions_Success() {
                // Given
                List<Subscription> subscriptions = Arrays.asList(testSubscription);
                when(subscriptionRepository.findByStatusWithUserAndPlan(Subscription.SubscriptionStatus.PENDING))
                                .thenReturn(subscriptions);

                // When
                List<SubscriptionResponse> responses = subscriptionService.getPendingSubscriptions();

                // Then
                assertNotNull(responses);
                assertEquals(1, responses.size());
                verify(subscriptionRepository).findByStatusWithUserAndPlan(Subscription.SubscriptionStatus.PENDING);
        }

        @Test
        void getSubscriptionsByStatus_Success() {
                // Given
                List<Subscription> subscriptions = Arrays.asList(testSubscription);
                when(subscriptionRepository.findByStatusWithUserAndPlan(Subscription.SubscriptionStatus.APPROVED))
                                .thenReturn(subscriptions);

                // When
                List<SubscriptionResponse> responses = subscriptionService
                                .getSubscriptionsByStatus(Subscription.SubscriptionStatus.APPROVED);

                // Then
                assertNotNull(responses);
                assertEquals(1, responses.size());
                verify(subscriptionRepository).findByStatusWithUserAndPlan(Subscription.SubscriptionStatus.APPROVED);
        }

        @Test
        void updateSubscriptionStatus_Approved_Success() {
                // Given
                SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest(
                                Subscription.SubscriptionStatus.APPROVED);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.updateSubscriptionStatus(subscriptionId,
                                adminUserId,
                                request);

                // Then
                assertNotNull(response);
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository).save(testSubscription);
        }

        @Test
        void updateSubscriptionStatus_Rejected_Success() {
                // Given
                SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest(
                                Subscription.SubscriptionStatus.REJECTED);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.updateSubscriptionStatus(subscriptionId,
                                adminUserId,
                                request);

                // Then
                assertNotNull(response);
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository).save(testSubscription);
        }

        @Test
        void updateSubscriptionStatus_Cancelled_Success() {
                // Given
                SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest(
                                Subscription.SubscriptionStatus.CANCELLED);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.updateSubscriptionStatus(subscriptionId,
                                adminUserId,
                                request);

                // Then
                assertNotNull(response);
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository).save(testSubscription);
        }

        @Test
        void updateSubscriptionStatus_InvalidStatus() {
                // Given
                SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest(
                                Subscription.SubscriptionStatus.PENDING);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.updateSubscriptionStatus(subscriptionId, adminUserId,
                                                request));
                assertEquals("Invalid status update", exception.getMessage());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void approveSubscription_Success() {
                // Given
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.approveSubscription(subscriptionId, adminUserId);

                // Then
                assertNotNull(response);
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository).save(testSubscription);
        }

        @Test
        void approveSubscription_NotPending() {
                // Given
                testSubscription.setStatus(Subscription.SubscriptionStatus.APPROVED);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.approveSubscription(subscriptionId, adminUserId));
                assertEquals("Only pending subscriptions can be approved", exception.getMessage());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void rejectSubscription_Success() {
                // Given
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.rejectSubscription(subscriptionId, adminUserId);

                // Then
                assertNotNull(response);
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository).save(testSubscription);
        }

        @Test
        void rejectSubscription_NotPending() {
                // Given
                testSubscription.setStatus(Subscription.SubscriptionStatus.APPROVED);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.rejectSubscription(subscriptionId, adminUserId));
                assertEquals("Only pending subscriptions can be rejected", exception.getMessage());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void cancelSubscription_Success() {
                // Given
                testSubscription.setStatus(Subscription.SubscriptionStatus.APPROVED);
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

                // When
                SubscriptionResponse response = subscriptionService.cancelSubscription(subscriptionId, userId);

                // Then
                assertNotNull(response);
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository).save(testSubscription);
        }

        @Test
        void cancelSubscription_NotOwner() {
                // Given
                UUID otherUserId = UUID.randomUUID();
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.cancelSubscription(subscriptionId, otherUserId));
                assertEquals("You can only cancel your own subscriptions", exception.getMessage());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void cancelSubscription_NotApproved() {
                // Given
                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> subscriptionService.cancelSubscription(subscriptionId, userId));
                assertEquals("Only approved subscriptions can be cancelled", exception.getMessage());
                verify(subscriptionRepository).findByIdWithUserAndPlan(subscriptionId);
                verify(subscriptionRepository, never()).save(any());
        }

        @Test
        void countActiveSubscriptionsByUserId_Success() {
                // Given
                when(subscriptionRepository.countActiveSubscriptionsByUserId(userId)).thenReturn(3L);

                // When
                long count = subscriptionService.countActiveSubscriptionsByUserId(userId);

                // Then
                assertEquals(3L, count);
                verify(subscriptionRepository).countActiveSubscriptionsByUserId(userId);
        }

        @Test
        void approveSubscription_ShouldCancelActiveSubscriptions_BeforeApproving() {
                // Given
                UUID adminUserId = UUID.randomUUID();
                testSubscription.setStatus(Subscription.SubscriptionStatus.PENDING);

                // Criar uma assinatura ativa existente
                Subscription activeSubscription = new Subscription();
                activeSubscription.setId(UUID.randomUUID());
                activeSubscription.setUser(testUser);
                activeSubscription.setStatus(Subscription.SubscriptionStatus.APPROVED);

                List<Subscription> activeSubscriptions = Arrays.asList(activeSubscription);

                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.findActiveSubscriptionsByUserId(userId))
                                .thenReturn(activeSubscriptions);
                when(subscriptionRepository.save(any(Subscription.class)))
                                .thenReturn(testSubscription);

                // When
                SubscriptionResponse result = subscriptionService.approveSubscription(subscriptionId, adminUserId);

                // Then
                assertNotNull(result);
                assertEquals(subscriptionId, result.getId());

                // Verifica se o método para buscar assinaturas ativas foi chamado
                verify(subscriptionRepository).findActiveSubscriptionsByUserId(userId);

                // Verifica se save foi chamado pelo menos 2 vezes: 1 para cancelar a ativa + 1
                // para aprovar a nova
                verify(subscriptionRepository, atLeast(2)).save(any(Subscription.class));
        }

        @Test
        void updateSubscriptionStatus_ToApproved_ShouldCancelActiveSubscriptions() {
                // Given
                UUID adminUserId = UUID.randomUUID();
                testSubscription.setStatus(Subscription.SubscriptionStatus.PENDING);

                SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest();
                request.setStatus(Subscription.SubscriptionStatus.APPROVED);

                // Criar uma assinatura ativa existente
                Subscription activeSubscription = new Subscription();
                activeSubscription.setId(UUID.randomUUID());
                activeSubscription.setUser(testUser);
                activeSubscription.setStatus(Subscription.SubscriptionStatus.APPROVED);

                List<Subscription> activeSubscriptions = Arrays.asList(activeSubscription);

                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.findActiveSubscriptionsByUserId(userId))
                                .thenReturn(activeSubscriptions);
                when(subscriptionRepository.save(any(Subscription.class)))
                                .thenReturn(testSubscription);

                // When
                SubscriptionResponse result = subscriptionService.updateSubscriptionStatus(subscriptionId, adminUserId,
                                request);

                // Then
                assertNotNull(result);
                assertEquals(subscriptionId, result.getId());

                // Verifica se o método para buscar assinaturas ativas foi chamado
                verify(subscriptionRepository).findActiveSubscriptionsByUserId(userId);

                // Verifica se save foi chamado pelo menos 2 vezes: 1 para cancelar a ativa + 1
                // para aprovar a nova
                verify(subscriptionRepository, atLeast(2)).save(any(Subscription.class));
        }

        @Test
        void updateSubscriptionStatus_ToRejected_ShouldNotCancelActiveSubscriptions() {
                // Given
                UUID adminUserId = UUID.randomUUID();
                testSubscription.setStatus(Subscription.SubscriptionStatus.PENDING);

                SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest();
                request.setStatus(Subscription.SubscriptionStatus.REJECTED);

                when(subscriptionRepository.findByIdWithUserAndPlan(subscriptionId))
                                .thenReturn(Optional.of(testSubscription));
                when(subscriptionRepository.save(any(Subscription.class)))
                                .thenReturn(testSubscription);

                // When
                SubscriptionResponse result = subscriptionService.updateSubscriptionStatus(subscriptionId, adminUserId,
                                request);

                // Then
                assertNotNull(result);
                assertEquals(subscriptionId, result.getId());

                // Verifica se o método para buscar assinaturas ativas NÃO foi chamado
                verify(subscriptionRepository, never()).findActiveSubscriptionsByUserId(any());

                // Verifica se save foi chamado apenas 1 vez para rejeitar
                verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        }
}
