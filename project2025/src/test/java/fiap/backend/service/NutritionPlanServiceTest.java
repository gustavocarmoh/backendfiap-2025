package fiap.backend.service;

import fiap.backend.domain.NutritionPlan;
import fiap.backend.domain.Subscription;
import fiap.backend.domain.SubscriptionPlan;
import fiap.backend.domain.User;
import fiap.backend.dto.CreateNutritionPlanDTO;
import fiap.backend.dto.NutritionPlanResponseDTO;
import fiap.backend.dto.UpdateNutritionPlanDTO;
import fiap.backend.dto.WeeklyNutritionPlanResponseDTO;
import fiap.backend.repository.NutritionPlanRepository;
import fiap.backend.repository.SubscriptionRepository;
import fiap.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionPlanServiceTest {

    @Mock
    private NutritionPlanRepository nutritionPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private NutritionPlanService nutritionPlanService;

    private User testUser;
    private SubscriptionPlan testPlan;
    private Subscription testSubscription;
    private NutritionPlan testNutritionPlan;
    private CreateNutritionPlanDTO createDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup subscription plan
        testPlan = new SubscriptionPlan();
        testPlan.setId(UUID.randomUUID());
        testPlan.setName("Premium");
        testPlan.setPrice(BigDecimal.valueOf(99.99));
        testPlan.setNutritionPlansLimit(50);
        testPlan.setIsActive(true);

        // Setup subscription
        testSubscription = new Subscription();
        testSubscription.setId(UUID.randomUUID());
        testSubscription.setUser(testUser);
        testSubscription.setPlan(testPlan);
        testSubscription.setStatus(Subscription.SubscriptionStatus.APPROVED);

        // Setup nutrition plan
        testNutritionPlan = new NutritionPlan();
        testNutritionPlan.setId(UUID.randomUUID());
        testNutritionPlan.setUser(testUser);
        testNutritionPlan.setPlanDate(LocalDate.now());
        testNutritionPlan.setTitle("Plano de Hoje");
        testNutritionPlan.setDescription("Plano nutricional para hoje");
        testNutritionPlan.setBreakfast("Aveia com frutas");
        testNutritionPlan.setTotalCalories(2000);
        testNutritionPlan.setIsCompleted(false);
        testNutritionPlan.setCreatedAt(LocalDateTime.now());
        testNutritionPlan.setUpdatedAt(LocalDateTime.now());

        // Setup DTO
        createDTO = new CreateNutritionPlanDTO();
        createDTO.setPlanDate(LocalDate.now().plusDays(1));
        createDTO.setTitle("Novo Plano");
        createDTO.setDescription("Descrição do novo plano");
        createDTO.setBreakfast("Café da manhã");
        createDTO.setTotalCalories(1800);
    }

    @Test
    void testCreateNutritionPlan_Success() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findActiveSubscriptionsByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testSubscription));
        when(nutritionPlanRepository.countByUser(testUser)).thenReturn(5L);
        when(nutritionPlanRepository.existsByUserAndPlanDate(testUser, createDTO.getPlanDate()))
                .thenReturn(false);
        when(nutritionPlanRepository.save(any(NutritionPlan.class))).thenReturn(testNutritionPlan);

        // When
        NutritionPlanResponseDTO result = nutritionPlanService.createNutritionPlan(
                testUser.getEmail(), createDTO);

        // Then
        assertNotNull(result);
        assertEquals(testNutritionPlan.getId(), result.getId());
        assertEquals(testNutritionPlan.getTitle(), result.getTitle());
        verify(nutritionPlanRepository).save(any(NutritionPlan.class));
    }

    @Test
    void testCreateNutritionPlan_UserNotFound() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            nutritionPlanService.createNutritionPlan(testUser.getEmail(), createDTO);
        });
    }

    @Test
    void testCreateNutritionPlan_NoActiveSubscription() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findActiveSubscriptionsByUserId(testUser.getId()))
                .thenReturn(Arrays.asList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nutritionPlanService.createNutritionPlan(testUser.getEmail(), createDTO);
        });
        assertEquals("Usuário não possui assinatura ativa", exception.getMessage());
    }

    @Test
    void testCreateNutritionPlan_LimitExceeded() {
        // Given
        testPlan.setNutritionPlansLimit(10);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findActiveSubscriptionsByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testSubscription));
        when(nutritionPlanRepository.countByUser(testUser)).thenReturn(10L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nutritionPlanService.createNutritionPlan(testUser.getEmail(), createDTO);
        });
        assertTrue(exception.getMessage().contains("Limite de planos nutricionais atingido"));
    }

    @Test
    void testCreateNutritionPlan_UnlimitedPlan() {
        // Given
        testPlan.setNutritionPlansLimit(null); // Unlimited
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findActiveSubscriptionsByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testSubscription));
        when(nutritionPlanRepository.existsByUserAndPlanDate(testUser, createDTO.getPlanDate()))
                .thenReturn(false);
        when(nutritionPlanRepository.save(any(NutritionPlan.class))).thenReturn(testNutritionPlan);

        // When
        NutritionPlanResponseDTO result = nutritionPlanService.createNutritionPlan(
                testUser.getEmail(), createDTO);

        // Then
        assertNotNull(result);
        verify(nutritionPlanRepository, never()).countByUser(testUser);
        verify(nutritionPlanRepository).save(any(NutritionPlan.class));
    }

    @Test
    void testCreateNutritionPlan_PlanAlreadyExists() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findActiveSubscriptionsByUserId(testUser.getId()))
                .thenReturn(Arrays.asList(testSubscription));
        when(nutritionPlanRepository.countByUser(testUser)).thenReturn(5L);
        when(nutritionPlanRepository.existsByUserAndPlanDate(testUser, createDTO.getPlanDate()))
                .thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nutritionPlanService.createNutritionPlan(testUser.getEmail(), createDTO);
        });
        assertEquals("Já existe um plano nutricional para esta data", exception.getMessage());
    }

    @Test
    void testGetTodayNutritionPlan_Found() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findTodayPlan(eq(testUser), any(LocalDate.class)))
                .thenReturn(Optional.of(testNutritionPlan));

        // When
        Optional<NutritionPlanResponseDTO> result = nutritionPlanService.getTodayNutritionPlan(testUser.getEmail());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testNutritionPlan.getId(), result.get().getId());
    }

    @Test
    void testGetTodayNutritionPlan_NotFound() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findTodayPlan(eq(testUser), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // When
        Optional<NutritionPlanResponseDTO> result = nutritionPlanService.getTodayNutritionPlan(testUser.getEmail());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWeeklyNutritionPlans() {
        // Given
        List<NutritionPlan> weekPlans = Arrays.asList(testNutritionPlan);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findWeekPlans(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(weekPlans);

        // When
        WeeklyNutritionPlanResponseDTO result = nutritionPlanService.getWeeklyNutritionPlans(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertNotNull(result.getPlans());
        assertEquals(1, result.getPlans().size());
        assertNotNull(result.getWeeklyStats());
        assertEquals(1, result.getWeeklyStats().getTotalPlans());
    }

    @Test
    void testGetUserNutritionPlans() {
        // Given
        List<NutritionPlan> plans = Arrays.asList(testNutritionPlan);
        Page<NutritionPlan> page = new PageImpl<>(plans);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findByUserOrderByPlanDateDesc(eq(testUser), any(PageRequest.class)))
                .thenReturn(page);

        // When
        Page<NutritionPlanResponseDTO> result = nutritionPlanService.getUserNutritionPlans(
                testUser.getEmail(), 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testNutritionPlan.getId(), result.getContent().get(0).getId());
    }

    @Test
    void testUpdateNutritionPlan_Success() {
        // Given
        UpdateNutritionPlanDTO updateDTO = new UpdateNutritionPlanDTO();
        updateDTO.setTitle("Título Atualizado");
        updateDTO.setBreakfast("Novo café da manhã");

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findById(testNutritionPlan.getId()))
                .thenReturn(Optional.of(testNutritionPlan));
        when(nutritionPlanRepository.save(testNutritionPlan)).thenReturn(testNutritionPlan);

        // When
        NutritionPlanResponseDTO result = nutritionPlanService.updateNutritionPlan(
                testUser.getEmail(), testNutritionPlan.getId(), updateDTO);

        // Then
        assertNotNull(result);
        verify(nutritionPlanRepository).save(testNutritionPlan);
    }

    @Test
    void testTogglePlanCompletion() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findById(testNutritionPlan.getId()))
                .thenReturn(Optional.of(testNutritionPlan));
        when(nutritionPlanRepository.save(testNutritionPlan)).thenReturn(testNutritionPlan);

        // When
        NutritionPlanResponseDTO result = nutritionPlanService.togglePlanCompletion(
                testUser.getEmail(), testNutritionPlan.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompleted()); // Should be marked as completed
        verify(nutritionPlanRepository).save(testNutritionPlan);
    }

    @Test
    void testDeleteNutritionPlan_Success() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findById(testNutritionPlan.getId()))
                .thenReturn(Optional.of(testNutritionPlan));

        // When
        nutritionPlanService.deleteNutritionPlan(testUser.getEmail(), testNutritionPlan.getId());

        // Then
        verify(nutritionPlanRepository).delete(testNutritionPlan);
    }

    @Test
    void testDeleteNutritionPlan_AccessDenied() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        testNutritionPlan.setUser(anotherUser);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(nutritionPlanRepository.findById(testNutritionPlan.getId()))
                .thenReturn(Optional.of(testNutritionPlan));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nutritionPlanService.deleteNutritionPlan(testUser.getEmail(), testNutritionPlan.getId());
        });
        assertEquals("Acesso negado ao plano nutricional", exception.getMessage());
    }
}
