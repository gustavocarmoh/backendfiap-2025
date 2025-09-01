package fiap.backend.service;

import fiap.backend.domain.Subscription;
import fiap.backend.dto.AdminDashboardDTO;
import fiap.backend.dto.GrowthMetricsDTO;
import fiap.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private NutritionPlanRepository nutritionPlanRepository;

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @Mock
    private UserPhotoRepository userPhotoRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        // Setup common mock returns for dashboard generation
        when(userRepository.count()).thenReturn(1000L);
        when(userRepository.countActiveUsers(any(LocalDateTime.class))).thenReturn(750L);
        when(userRepository.countUsersByPeriod(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(50L);

        when(subscriptionRepository.count()).thenReturn(300L);
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.APPROVED)).thenReturn(250L);
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.PENDING)).thenReturn(30L);
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.REJECTED)).thenReturn(20L);
        when(subscriptionRepository.calculateTotalRevenue()).thenReturn(BigDecimal.valueOf(25000.00));
        when(subscriptionRepository.calculateMonthlyRevenue(any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(3000.00));

        when(nutritionPlanRepository.count()).thenReturn(2000L);
        when(nutritionPlanRepository.countByIsCompleted(true)).thenReturn(1600L);
        when(nutritionPlanRepository.countByPeriod(any(LocalDate.class), any(LocalDate.class))).thenReturn(200L);

        when(serviceProviderRepository.count()).thenReturn(150L);
        when(serviceProviderRepository.countByIsActive(true)).thenReturn(120L);
        when(serviceProviderRepository.countByPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(15L);

        when(userPhotoRepository.count()).thenReturn(800L);
        when(userPhotoRepository.countByPeriod(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(80L);

        when(subscriptionPlanRepository.findByIsActive(true)).thenReturn(new ArrayList<>());
    }

    @Test
    void testGenerateDashboard_Success() {
        // When
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();

        // Then
        assertNotNull(dashboard);

        // Verify user metrics
        assertEquals(1000L, dashboard.getTotalUsers());
        assertEquals(750L, dashboard.getActiveUsers());
        assertEquals(50L, dashboard.getNewUsersThisMonth());

        // Verify subscription metrics
        assertEquals(300L, dashboard.getTotalSubscriptions());
        assertEquals(250L, dashboard.getActiveSubscriptions());
        assertEquals(30L, dashboard.getPendingSubscriptions());
        assertEquals(20L, dashboard.getRejectedSubscriptions());
        assertEquals(BigDecimal.valueOf(25000.00), dashboard.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(3000.00), dashboard.getMonthlyRevenue());

        // Verify conversion rate calculation
        assertEquals(25.0, dashboard.getSubscriptionConversionRate());

        // Verify nutrition plan metrics
        assertEquals(2000L, dashboard.getTotalNutritionPlans());
        assertEquals(1600L, dashboard.getCompletedNutritionPlans());
        assertEquals(200L, dashboard.getNutritionPlansThisMonth());
        assertEquals(80.0, dashboard.getNutritionPlanCompletionRate());
        assertEquals(2.0, dashboard.getAverageNutritionPlansPerUser());

        // Verify service provider metrics
        assertEquals(150L, dashboard.getTotalServiceProviders());
        assertEquals(120L, dashboard.getActiveServiceProviders());
        assertEquals(15L, dashboard.getServiceProvidersThisMonth());

        // Verify engagement metrics
        assertEquals(800L, dashboard.getTotalUserPhotos());
        assertEquals(80L, dashboard.getPhotosUploadedThisMonth());
        assertEquals(0.8, dashboard.getAveragePhotosPerUser());

        // Verify report date is set
        assertNotNull(dashboard.getReportDate());
        assertEquals(LocalDate.now(), dashboard.getReportDate());
    }

    @Test
    void testGenerateDashboard_WithZeroUsers() {
        // Given
        when(userRepository.count()).thenReturn(0L);

        // When
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();

        // Then
        assertNotNull(dashboard);
        assertEquals(0L, dashboard.getTotalUsers());
        assertEquals(0.0, dashboard.getSubscriptionConversionRate());
        assertEquals(0.0, dashboard.getAverageNutritionPlansPerUser());
        assertEquals(0.0, dashboard.getAveragePhotosPerUser());
    }

    @Test
    void testGenerateDashboard_WithNullRevenue() {
        // Given
        when(subscriptionRepository.calculateTotalRevenue()).thenReturn(null);
        when(subscriptionRepository.calculateMonthlyRevenue(any(LocalDateTime.class))).thenReturn(null);

        // When
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();

        // Then
        assertNotNull(dashboard);
        assertEquals(BigDecimal.ZERO, dashboard.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, dashboard.getMonthlyRevenue());
    }

    @Test
    void testGenerateGrowthMetrics_Daily() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        String period = "daily";

        when(userRepository.countUsersByDate(any(LocalDate.class))).thenReturn(5L);
        when(subscriptionRepository.countSubscriptionsByDate(any(LocalDate.class))).thenReturn(2L);
        when(subscriptionRepository.calculateDailyRevenue(any(LocalDate.class))).thenReturn(BigDecimal.valueOf(100.00));
        when(nutritionPlanRepository.countByDate(any(LocalDate.class))).thenReturn(10L);

        // When
        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, period);

        // Then
        assertNotNull(growthMetrics);
        assertEquals(startDate, growthMetrics.getStartDate());
        assertEquals(endDate, growthMetrics.getEndDate());
        assertEquals("daily", growthMetrics.getPeriod());

        assertNotNull(growthMetrics.getUserGrowth());
        assertNotNull(growthMetrics.getSubscriptionGrowth());
        assertNotNull(growthMetrics.getRevenueGrowth());
        assertNotNull(growthMetrics.getNutritionPlanGrowth());

        // Should have data for each day in the range (8 days including start and end)
        assertEquals(8, growthMetrics.getUserGrowth().size());
        assertEquals(8, growthMetrics.getSubscriptionGrowth().size());
        assertEquals(8, growthMetrics.getRevenueGrowth().size());
        assertEquals(8, growthMetrics.getNutritionPlanGrowth().size());
    }

    @Test
    void testGenerateGrowthMetrics_Weekly() {
        // Given
        LocalDate startDate = LocalDate.now().minusWeeks(4);
        LocalDate endDate = LocalDate.now();
        String period = "weekly";

        when(userRepository.countUsersByDate(any(LocalDate.class))).thenReturn(5L);
        when(subscriptionRepository.countSubscriptionsByDate(any(LocalDate.class))).thenReturn(2L);
        when(subscriptionRepository.calculateDailyRevenue(any(LocalDate.class))).thenReturn(BigDecimal.valueOf(100.00));
        when(nutritionPlanRepository.countByDate(any(LocalDate.class))).thenReturn(10L);

        // When
        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, period);

        // Then
        assertNotNull(growthMetrics);
        assertEquals("weekly", growthMetrics.getPeriod());
        assertNotNull(growthMetrics.getUserGrowth());
        assertNotNull(growthMetrics.getSubscriptionGrowth());
        assertNotNull(growthMetrics.getRevenueGrowth());
        assertNotNull(growthMetrics.getNutritionPlanGrowth());
    }

    @Test
    void testGenerateGrowthMetrics_Monthly() {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(12);
        LocalDate endDate = LocalDate.now();
        String period = "monthly";

        when(userRepository.countUsersByDate(any(LocalDate.class))).thenReturn(5L);
        when(subscriptionRepository.countSubscriptionsByDate(any(LocalDate.class))).thenReturn(2L);
        when(subscriptionRepository.calculateDailyRevenue(any(LocalDate.class))).thenReturn(BigDecimal.valueOf(100.00));
        when(nutritionPlanRepository.countByDate(any(LocalDate.class))).thenReturn(10L);

        // When
        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, period);

        // Then
        assertNotNull(growthMetrics);
        assertEquals("monthly", growthMetrics.getPeriod());
        assertNotNull(growthMetrics.getUserGrowth());
    }

    @Test
    void testCalculateUserGrowthRate() {
        // Given - setup for growth rate calculation
        when(userRepository.countUsersByPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(40L) // last month
                .thenReturn(50L); // this month

        // When
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();

        // Then
        assertNotNull(dashboard.getUserGrowthRate());
        assertEquals(25.0, dashboard.getUserGrowthRate()); // (50-40)/40 * 100 = 25%
    }

    @Test
    void testNutritionPlanCompletionRate() {
        // Given
        when(nutritionPlanRepository.count()).thenReturn(1000L);
        when(nutritionPlanRepository.countByIsCompleted(true)).thenReturn(850L);

        // When
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();

        // Then
        assertEquals(85.0, dashboard.getNutritionPlanCompletionRate());
    }

    @Test
    void testConversionRateCalculation() {
        // Given
        when(userRepository.count()).thenReturn(500L);
        when(subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.APPROVED)).thenReturn(125L);

        // When
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();

        // Then
        assertEquals(25.0, dashboard.getSubscriptionConversionRate()); // 125/500 * 100 = 25%
    }

    @Test
    void testGenerateGrowthMetrics_WithNullRevenue() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        when(userRepository.countUsersByDate(any(LocalDate.class))).thenReturn(5L);
        when(subscriptionRepository.countSubscriptionsByDate(any(LocalDate.class))).thenReturn(2L);
        when(subscriptionRepository.calculateDailyRevenue(any(LocalDate.class))).thenReturn(null);
        when(nutritionPlanRepository.countByDate(any(LocalDate.class))).thenReturn(10L);

        // When
        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, "daily");

        // Then
        assertNotNull(growthMetrics);
        assertNotNull(growthMetrics.getRevenueGrowth());

        // Should handle null revenue gracefully by setting to zero
        GrowthMetricsDTO.PeriodMetricDTO revenueMetric = growthMetrics.getRevenueGrowth().get(0);
        assertEquals(BigDecimal.ZERO, revenueMetric.getValue());
    }

    @Test
    void testRepositoryInteractions() {
        // When
        adminDashboardService.generateDashboard();

        // Then - verify all repositories are called
        verify(userRepository).count();
        verify(userRepository).countActiveUsers(any(LocalDateTime.class));
        verify(userRepository, times(2)).countUsersByPeriod(any(LocalDateTime.class), any(LocalDateTime.class));

        verify(subscriptionRepository).count();
        verify(subscriptionRepository, times(3)).countByStatus(any(Subscription.SubscriptionStatus.class));
        verify(subscriptionRepository).calculateTotalRevenue();
        verify(subscriptionRepository).calculateMonthlyRevenue(any(LocalDateTime.class));

        verify(nutritionPlanRepository).count();
        verify(nutritionPlanRepository).countByIsCompleted(true);
        verify(nutritionPlanRepository).countByPeriod(any(LocalDate.class), any(LocalDate.class));

        verify(serviceProviderRepository).count();
        verify(serviceProviderRepository).countByIsActive(true);
        verify(serviceProviderRepository).countByPeriod(any(LocalDateTime.class), any(LocalDateTime.class));

        verify(userPhotoRepository).count();
        verify(userPhotoRepository).countByPeriod(any(LocalDateTime.class), any(LocalDateTime.class));

        verify(subscriptionPlanRepository).findByIsActive(true);
    }
}
