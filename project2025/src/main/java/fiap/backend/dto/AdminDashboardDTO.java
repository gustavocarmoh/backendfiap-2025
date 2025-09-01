package fiap.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para dashboard administrativo com métricas de negócio
 */
public class AdminDashboardDTO {

    // Métricas gerais de usuários
    private Long totalUsers;
    private Long activeUsers; // Usuários com login nos últimos 30 dias
    private Long newUsersThisMonth;
    private Double userGrowthRate; // Taxa de crescimento mensal

    // Métricas de assinaturas
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long pendingSubscriptions;
    private Long rejectedSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Double subscriptionConversionRate; // Taxa de conversão de usuários para assinantes

    // Métricas de planos nutricionais
    private Long totalNutritionPlans;
    private Long completedNutritionPlans;
    private Long nutritionPlansThisMonth;
    private Double nutritionPlanCompletionRate;
    private Double averageNutritionPlansPerUser;

    // Métricas de fornecedores
    private Long totalServiceProviders;
    private Long activeServiceProviders;
    private Long serviceProvidersThisMonth;

    // Métricas de engajamento
    private Long totalUserPhotos;
    private Long photosUploadedThisMonth;
    private Double averagePhotosPerUser;

    // Métricas por plano de assinatura
    private SubscriptionPlanMetricsDTO basicPlanMetrics;
    private SubscriptionPlanMetricsDTO premiumPlanMetrics;
    private SubscriptionPlanMetricsDTO proPlanMetrics;

    // Data de referência para as métricas
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    // Construtores
    public AdminDashboardDTO() {
        this.reportDate = LocalDate.now();
    }

    // Getters e Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(Long newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public Double getUserGrowthRate() {
        return userGrowthRate;
    }

    public void setUserGrowthRate(Double userGrowthRate) {
        this.userGrowthRate = userGrowthRate;
    }

    public Long getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(Long totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public Long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Long activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public Long getPendingSubscriptions() {
        return pendingSubscriptions;
    }

    public void setPendingSubscriptions(Long pendingSubscriptions) {
        this.pendingSubscriptions = pendingSubscriptions;
    }

    public Long getRejectedSubscriptions() {
        return rejectedSubscriptions;
    }

    public void setRejectedSubscriptions(Long rejectedSubscriptions) {
        this.rejectedSubscriptions = rejectedSubscriptions;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public Double getSubscriptionConversionRate() {
        return subscriptionConversionRate;
    }

    public void setSubscriptionConversionRate(Double subscriptionConversionRate) {
        this.subscriptionConversionRate = subscriptionConversionRate;
    }

    public Long getTotalNutritionPlans() {
        return totalNutritionPlans;
    }

    public void setTotalNutritionPlans(Long totalNutritionPlans) {
        this.totalNutritionPlans = totalNutritionPlans;
    }

    public Long getCompletedNutritionPlans() {
        return completedNutritionPlans;
    }

    public void setCompletedNutritionPlans(Long completedNutritionPlans) {
        this.completedNutritionPlans = completedNutritionPlans;
    }

    public Long getNutritionPlansThisMonth() {
        return nutritionPlansThisMonth;
    }

    public void setNutritionPlansThisMonth(Long nutritionPlansThisMonth) {
        this.nutritionPlansThisMonth = nutritionPlansThisMonth;
    }

    public Double getNutritionPlanCompletionRate() {
        return nutritionPlanCompletionRate;
    }

    public void setNutritionPlanCompletionRate(Double nutritionPlanCompletionRate) {
        this.nutritionPlanCompletionRate = nutritionPlanCompletionRate;
    }

    public Double getAverageNutritionPlansPerUser() {
        return averageNutritionPlansPerUser;
    }

    public void setAverageNutritionPlansPerUser(Double averageNutritionPlansPerUser) {
        this.averageNutritionPlansPerUser = averageNutritionPlansPerUser;
    }

    public Long getTotalServiceProviders() {
        return totalServiceProviders;
    }

    public void setTotalServiceProviders(Long totalServiceProviders) {
        this.totalServiceProviders = totalServiceProviders;
    }

    public Long getActiveServiceProviders() {
        return activeServiceProviders;
    }

    public void setActiveServiceProviders(Long activeServiceProviders) {
        this.activeServiceProviders = activeServiceProviders;
    }

    public Long getServiceProvidersThisMonth() {
        return serviceProvidersThisMonth;
    }

    public void setServiceProvidersThisMonth(Long serviceProvidersThisMonth) {
        this.serviceProvidersThisMonth = serviceProvidersThisMonth;
    }

    public Long getTotalUserPhotos() {
        return totalUserPhotos;
    }

    public void setTotalUserPhotos(Long totalUserPhotos) {
        this.totalUserPhotos = totalUserPhotos;
    }

    public Long getPhotosUploadedThisMonth() {
        return photosUploadedThisMonth;
    }

    public void setPhotosUploadedThisMonth(Long photosUploadedThisMonth) {
        this.photosUploadedThisMonth = photosUploadedThisMonth;
    }

    public Double getAveragePhotosPerUser() {
        return averagePhotosPerUser;
    }

    public void setAveragePhotosPerUser(Double averagePhotosPerUser) {
        this.averagePhotosPerUser = averagePhotosPerUser;
    }

    public SubscriptionPlanMetricsDTO getBasicPlanMetrics() {
        return basicPlanMetrics;
    }

    public void setBasicPlanMetrics(SubscriptionPlanMetricsDTO basicPlanMetrics) {
        this.basicPlanMetrics = basicPlanMetrics;
    }

    public SubscriptionPlanMetricsDTO getPremiumPlanMetrics() {
        return premiumPlanMetrics;
    }

    public void setPremiumPlanMetrics(SubscriptionPlanMetricsDTO premiumPlanMetrics) {
        this.premiumPlanMetrics = premiumPlanMetrics;
    }

    public SubscriptionPlanMetricsDTO getProPlanMetrics() {
        return proPlanMetrics;
    }

    public void setProPlanMetrics(SubscriptionPlanMetricsDTO proPlanMetrics) {
        this.proPlanMetrics = proPlanMetrics;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    /**
     * Classe interna para métricas por plano de assinatura
     */
    public static class SubscriptionPlanMetricsDTO {
        private String planName;
        private BigDecimal planPrice;
        private Long subscriberCount;
        private BigDecimal revenue;
        private Double marketShare; // Percentual de participação
        private Long nutritionPlansCreated;
        private Double avgNutritionPlansPerSubscriber;

        // Construtores
        public SubscriptionPlanMetricsDTO() {
        }

        public SubscriptionPlanMetricsDTO(String planName, BigDecimal planPrice) {
            this.planName = planName;
            this.planPrice = planPrice;
        }

        // Getters e Setters
        public String getPlanName() {
            return planName;
        }

        public void setPlanName(String planName) {
            this.planName = planName;
        }

        public BigDecimal getPlanPrice() {
            return planPrice;
        }

        public void setPlanPrice(BigDecimal planPrice) {
            this.planPrice = planPrice;
        }

        public Long getSubscriberCount() {
            return subscriberCount;
        }

        public void setSubscriberCount(Long subscriberCount) {
            this.subscriberCount = subscriberCount;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public Double getMarketShare() {
            return marketShare;
        }

        public void setMarketShare(Double marketShare) {
            this.marketShare = marketShare;
        }

        public Long getNutritionPlansCreated() {
            return nutritionPlansCreated;
        }

        public void setNutritionPlansCreated(Long nutritionPlansCreated) {
            this.nutritionPlansCreated = nutritionPlansCreated;
        }

        public Double getAvgNutritionPlansPerSubscriber() {
            return avgNutritionPlansPerSubscriber;
        }

        public void setAvgNutritionPlansPerSubscriber(Double avgNutritionPlansPerSubscriber) {
            this.avgNutritionPlansPerSubscriber = avgNutritionPlansPerSubscriber;
        }
    }
}
