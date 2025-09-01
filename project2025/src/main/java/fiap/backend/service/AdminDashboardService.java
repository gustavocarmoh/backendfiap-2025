package fiap.backend.service;

import fiap.backend.domain.Subscription;
import fiap.backend.dto.AdminDashboardDTO;
import fiap.backend.dto.GrowthMetricsDTO;
import fiap.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para geração de métricas do dashboard administrativo
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private UserPhotoRepository userPhotoRepository;

    /**
     * Gera dashboard completo com todas as métricas
     */
    public AdminDashboardDTO generateDashboard() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // Métricas de usuários
        generateUserMetrics(dashboard);

        // Métricas de assinaturas
        generateSubscriptionMetrics(dashboard);

        // Métricas de planos nutricionais
        generateNutritionPlanMetrics(dashboard);

        // Métricas de fornecedores
        generateServiceProviderMetrics(dashboard);

        // Métricas de engajamento
        generateEngagementMetrics(dashboard);

        // Métricas por plano de assinatura
        generateSubscriptionPlanMetrics(dashboard);

        return dashboard;
    }

    /**
     * Gera métricas de crescimento por período
     */
    public GrowthMetricsDTO generateGrowthMetrics(LocalDate startDate, LocalDate endDate, String period) {
        GrowthMetricsDTO growthMetrics = new GrowthMetricsDTO(startDate, endDate, period);

        // Gerar métricas baseadas no período solicitado
        switch (period.toLowerCase()) {
            case "monthly":
                generateMonthlyGrowthMetrics(growthMetrics, startDate, endDate);
                break;
            case "weekly":
                generateWeeklyGrowthMetrics(growthMetrics, startDate, endDate);
                break;
            case "daily":
            default:
                generateDailyGrowthMetrics(growthMetrics, startDate, endDate);
                break;
        }

        return growthMetrics;
    }

    /**
     * Gera métricas de usuários
     */
    private void generateUserMetrics(AdminDashboardDTO dashboard) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Total de usuários
        Long totalUsers = userRepository.count();
        dashboard.setTotalUsers(totalUsers);

        // Usuários ativos (com login nos últimos 30 dias)
        Long activeUsers = userRepository.countActiveUsers(thirtyDaysAgo);
        dashboard.setActiveUsers(activeUsers);

        // Novos usuários este mês
        Long newUsersThisMonth = userRepository.countUsersByPeriod(startOfMonth.atStartOfDay(), now.atTime(23, 59, 59));
        dashboard.setNewUsersThisMonth(newUsersThisMonth);

        // Taxa de crescimento mensal
        Long newUsersLastMonth = userRepository.countUsersByPeriod(startOfLastMonth.atStartOfDay(),
                startOfMonth.atStartOfDay());
        Double userGrowthRate = calculateGrowthRate(newUsersLastMonth, newUsersThisMonth);
        dashboard.setUserGrowthRate(userGrowthRate);
    }

    /**
     * Gera métricas de assinaturas
     */
    private void generateSubscriptionMetrics(AdminDashboardDTO dashboard) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        // Contagens por status
        Long totalSubscriptions = subscriptionRepository.count();
        Long activeSubscriptions = subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.APPROVED);
        Long pendingSubscriptions = subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.PENDING);
        Long rejectedSubscriptions = subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.REJECTED);

        dashboard.setTotalSubscriptions(totalSubscriptions);
        dashboard.setActiveSubscriptions(activeSubscriptions);
        dashboard.setPendingSubscriptions(pendingSubscriptions);
        dashboard.setRejectedSubscriptions(rejectedSubscriptions);

        // Receita total e mensal
        BigDecimal totalRevenue = subscriptionRepository.calculateTotalRevenue();
        BigDecimal monthlyRevenue = subscriptionRepository.calculateMonthlyRevenue(startOfMonth.atStartOfDay());

        dashboard.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        dashboard.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);

        // Taxa de conversão (assinantes ativos / total de usuários)
        if (dashboard.getTotalUsers() > 0) {
            Double conversionRate = (activeSubscriptions.doubleValue() / dashboard.getTotalUsers().doubleValue()) * 100;
            dashboard.setSubscriptionConversionRate(conversionRate);
        } else {
            dashboard.setSubscriptionConversionRate(0.0);
        }
    }

    /**
     * Gera métricas de planos nutricionais
     */
    private void generateNutritionPlanMetrics(AdminDashboardDTO dashboard) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        // Contagens de planos nutricionais
        Long totalNutritionPlans = nutritionPlanRepository.count();
        Long completedNutritionPlans = nutritionPlanRepository.countByIsCompleted(true);
        Long nutritionPlansThisMonth = nutritionPlanRepository.countByPeriod(startOfMonth, now);

        dashboard.setTotalNutritionPlans(totalNutritionPlans);
        dashboard.setCompletedNutritionPlans(completedNutritionPlans);
        dashboard.setNutritionPlansThisMonth(nutritionPlansThisMonth);

        // Taxa de conclusão
        if (totalNutritionPlans > 0) {
            Double completionRate = (completedNutritionPlans.doubleValue() / totalNutritionPlans.doubleValue()) * 100;
            dashboard.setNutritionPlanCompletionRate(completionRate);
        } else {
            dashboard.setNutritionPlanCompletionRate(0.0);
        }

        // Média de planos por usuário
        if (dashboard.getTotalUsers() > 0) {
            Double avgPlansPerUser = totalNutritionPlans.doubleValue() / dashboard.getTotalUsers().doubleValue();
            dashboard.setAverageNutritionPlansPerUser(avgPlansPerUser);
        } else {
            dashboard.setAverageNutritionPlansPerUser(0.0);
        }
    }

    /**
     * Gera métricas de fornecedores de serviço
     */
    private void generateServiceProviderMetrics(AdminDashboardDTO dashboard) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        Long totalServiceProviders = serviceProviderRepository.count();
        Long activeServiceProviders = serviceProviderRepository.countByIsActive(true);
        Long serviceProvidersThisMonth = serviceProviderRepository.countByPeriod(startOfMonth.atStartOfDay(),
                now.atTime(23, 59, 59));

        dashboard.setTotalServiceProviders(totalServiceProviders);
        dashboard.setActiveServiceProviders(activeServiceProviders);
        dashboard.setServiceProvidersThisMonth(serviceProvidersThisMonth);
    }

    /**
     * Gera métricas de engajamento
     */
    private void generateEngagementMetrics(AdminDashboardDTO dashboard) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        Long totalUserPhotos = userPhotoRepository.count();
        Long photosUploadedThisMonth = userPhotoRepository.countByPeriod(startOfMonth.atStartOfDay(),
                now.atTime(23, 59, 59));

        dashboard.setTotalUserPhotos(totalUserPhotos);
        dashboard.setPhotosUploadedThisMonth(photosUploadedThisMonth);

        // Média de fotos por usuário
        if (dashboard.getTotalUsers() > 0) {
            Double avgPhotosPerUser = totalUserPhotos.doubleValue() / dashboard.getTotalUsers().doubleValue();
            dashboard.setAveragePhotosPerUser(avgPhotosPerUser);
        } else {
            dashboard.setAveragePhotosPerUser(0.0);
        }
    }

    /**
     * Gera métricas por plano de assinatura
     */
    private void generateSubscriptionPlanMetrics(AdminDashboardDTO dashboard) {
        // Buscar todos os planos de assinatura ativos
        var subscriptionPlans = subscriptionPlanRepository.findByIsActive(true);

        for (var plan : subscriptionPlans) {
            AdminDashboardDTO.SubscriptionPlanMetricsDTO planMetrics = new AdminDashboardDTO.SubscriptionPlanMetricsDTO(
                    plan.getName(), plan.getPrice());

            // Contar assinantes por plano
            Long subscriberCount = subscriptionRepository.countActiveSubscriptionsByPlan(plan.getId());
            planMetrics.setSubscriberCount(subscriberCount);

            // Calcular receita por plano
            BigDecimal planRevenue = plan.getPrice().multiply(BigDecimal.valueOf(subscriberCount));
            planMetrics.setRevenue(planRevenue);

            // Calcular participação de mercado
            if (dashboard.getActiveSubscriptions() > 0) {
                Double marketShare = (subscriberCount.doubleValue() / dashboard.getActiveSubscriptions().doubleValue())
                        * 100;
                planMetrics.setMarketShare(marketShare);
            }

            // Contar planos nutricionais criados pelos assinantes deste plano
            Long nutritionPlansCreated = nutritionPlanRepository.countBySubscriptionPlan(plan.getId());
            planMetrics.setNutritionPlansCreated(nutritionPlansCreated);

            // Média de planos nutricionais por assinante
            if (subscriberCount > 0) {
                Double avgNutritionPlans = nutritionPlansCreated.doubleValue() / subscriberCount.doubleValue();
                planMetrics.setAvgNutritionPlansPerSubscriber(avgNutritionPlans);
            }

            // Atribuir aos campos correspondentes do dashboard
            switch (plan.getName().toLowerCase()) {
                case "basic":
                    dashboard.setBasicPlanMetrics(planMetrics);
                    break;
                case "premium":
                    dashboard.setPremiumPlanMetrics(planMetrics);
                    break;
                case "pro":
                    dashboard.setProPlanMetrics(planMetrics);
                    break;
            }
        }
    }

    /**
     * Gera métricas de crescimento diário
     */
    private void generateDailyGrowthMetrics(GrowthMetricsDTO growthMetrics, LocalDate startDate, LocalDate endDate) {
        List<GrowthMetricsDTO.PeriodMetricDTO> userGrowth = new ArrayList<>();
        List<GrowthMetricsDTO.PeriodMetricDTO> subscriptionGrowth = new ArrayList<>();
        List<GrowthMetricsDTO.PeriodMetricDTO> revenueGrowth = new ArrayList<>();
        List<GrowthMetricsDTO.PeriodMetricDTO> nutritionPlanGrowth = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Novos usuários no dia
            Long dailyUsers = userRepository.countUsersByDate(currentDate);
            userGrowth.add(new GrowthMetricsDTO.PeriodMetricDTO(currentDate, currentDate.toString(), dailyUsers));

            // Novas assinaturas no dia
            Long dailySubscriptions = subscriptionRepository.countSubscriptionsByDate(currentDate);
            subscriptionGrowth
                    .add(new GrowthMetricsDTO.PeriodMetricDTO(currentDate, currentDate.toString(), dailySubscriptions));

            // Receita do dia
            BigDecimal dailyRevenue = subscriptionRepository.calculateDailyRevenue(currentDate);
            revenueGrowth.add(new GrowthMetricsDTO.PeriodMetricDTO(currentDate, currentDate.toString(),
                    dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO));

            // Planos nutricionais criados no dia
            Long dailyNutritionPlans = nutritionPlanRepository.countByDate(currentDate);
            nutritionPlanGrowth.add(
                    new GrowthMetricsDTO.PeriodMetricDTO(currentDate, currentDate.toString(), dailyNutritionPlans));

            currentDate = currentDate.plusDays(1);
        }

        growthMetrics.setUserGrowth(userGrowth);
        growthMetrics.setSubscriptionGrowth(subscriptionGrowth);
        growthMetrics.setRevenueGrowth(revenueGrowth);
        growthMetrics.setNutritionPlanGrowth(nutritionPlanGrowth);
    }

    /**
     * Gera métricas de crescimento semanal
     */
    private void generateWeeklyGrowthMetrics(GrowthMetricsDTO growthMetrics, LocalDate startDate, LocalDate endDate) {
        // Implementação similar ao diário, mas agrupando por semana
        // Por simplicidade, usando o método diário como base
        generateDailyGrowthMetrics(growthMetrics, startDate, endDate);
    }

    /**
     * Gera métricas de crescimento mensal
     */
    private void generateMonthlyGrowthMetrics(GrowthMetricsDTO growthMetrics, LocalDate startDate, LocalDate endDate) {
        // Implementação similar ao diário, mas agrupando por mês
        // Por simplicidade, usando o método diário como base
        generateDailyGrowthMetrics(growthMetrics, startDate, endDate);
    }

    /**
     * Calcula taxa de crescimento entre dois períodos
     */
    private Double calculateGrowthRate(Long previous, Long current) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }

        if (current == null) {
            return -100.0;
        }

        double rate = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
