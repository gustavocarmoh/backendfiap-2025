package fiap.backend.service;

import fiap.backend.domain.SubscriptionPlan;
import fiap.backend.dto.SubscriptionPlanRequest;
import fiap.backend.dto.SubscriptionPlanResponse;
import fiap.backend.repository.SubscriptionPlanRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks
    private SubscriptionPlanServiceImpl subscriptionPlanService;

    private SubscriptionPlan testPlan;
    private SubscriptionPlanRequest testRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testPlan = new SubscriptionPlan("Test Plan", BigDecimal.valueOf(19.99));
        testPlan.setId(testId);
        testPlan.setCreatedAt(LocalDateTime.now());
        testPlan.setUpdatedAt(LocalDateTime.now());
        testPlan.setIsActive(true);

        testRequest = new SubscriptionPlanRequest("Test Plan", BigDecimal.valueOf(19.99));
    }

    @Test
    void createPlan_Success() {
        // Given
        when(subscriptionPlanRepository.existsByName(anyString())).thenReturn(false);
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(testPlan);

        // When
        SubscriptionPlanResponse response = subscriptionPlanService.createPlan(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(testPlan.getName(), response.getName());
        assertEquals(testPlan.getPrice(), response.getPrice());
        assertTrue(response.getIsActive());
        verify(subscriptionPlanRepository).existsByName(testRequest.getName());
        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    void createPlan_ThrowsException_WhenNameExists() {
        // Given
        when(subscriptionPlanRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> subscriptionPlanService.createPlan(testRequest));
        assertEquals("A plan with this name already exists", exception.getMessage());
        verify(subscriptionPlanRepository).existsByName(testRequest.getName());
        verify(subscriptionPlanRepository, never()).save(any());
    }

    @Test
    void getPlanById_Success() {
        // Given
        when(subscriptionPlanRepository.findById(testId)).thenReturn(Optional.of(testPlan));

        // When
        SubscriptionPlanResponse response = subscriptionPlanService.getPlanById(testId);

        // Then
        assertNotNull(response);
        assertEquals(testPlan.getId(), response.getId());
        assertEquals(testPlan.getName(), response.getName());
        assertEquals(testPlan.getPrice(), response.getPrice());
        verify(subscriptionPlanRepository).findById(testId);
    }

    @Test
    void getPlanById_ThrowsException_WhenNotFound() {
        // Given
        when(subscriptionPlanRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> subscriptionPlanService.getPlanById(testId));
        assertEquals("Plan not found", exception.getMessage());
        verify(subscriptionPlanRepository).findById(testId);
    }

    @Test
    void getAllActivePlans_Success() {
        // Given
        List<SubscriptionPlan> activePlans = Arrays.asList(testPlan);
        when(subscriptionPlanRepository.findAllActivePlansOrderByPrice()).thenReturn(activePlans);

        // When
        List<SubscriptionPlanResponse> responses = subscriptionPlanService.getAllActivePlans();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testPlan.getName(), responses.get(0).getName());
        verify(subscriptionPlanRepository).findAllActivePlansOrderByPrice();
    }

    @Test
    void updatePlan_Success() {
        // Given
        SubscriptionPlanRequest updateRequest = new SubscriptionPlanRequest("Updated Plan", BigDecimal.valueOf(29.99));
        when(subscriptionPlanRepository.findById(testId)).thenReturn(Optional.of(testPlan));
        when(subscriptionPlanRepository.existsByName("Updated Plan")).thenReturn(false);
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(testPlan);

        // When
        SubscriptionPlanResponse response = subscriptionPlanService.updatePlan(testId, updateRequest);

        // Then
        assertNotNull(response);
        verify(subscriptionPlanRepository).findById(testId);
        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    void deactivatePlan_Success() {
        // Given
        when(subscriptionPlanRepository.findById(testId)).thenReturn(Optional.of(testPlan));
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(testPlan);

        // When
        subscriptionPlanService.deactivatePlan(testId);

        // Then
        verify(subscriptionPlanRepository).findById(testId);
        verify(subscriptionPlanRepository).save(testPlan);
    }

    @Test
    void deletePlan_Success() {
        // Given
        when(subscriptionPlanRepository.existsById(testId)).thenReturn(true);

        // When
        subscriptionPlanService.deletePlan(testId);

        // Then
        verify(subscriptionPlanRepository).existsById(testId);
        verify(subscriptionPlanRepository).deleteById(testId);
    }

    @Test
    void deletePlan_ThrowsException_WhenNotFound() {
        // Given
        when(subscriptionPlanRepository.existsById(testId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> subscriptionPlanService.deletePlan(testId));
        assertEquals("Plan not found", exception.getMessage());
        verify(subscriptionPlanRepository).existsById(testId);
        verify(subscriptionPlanRepository, never()).deleteById(any());
    }
}
