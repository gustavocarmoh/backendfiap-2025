package fiap.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para métricas de crescimento por período
 */
public class GrowthMetricsDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String period; // "daily", "weekly", "monthly"

    private List<PeriodMetricDTO> userGrowth;
    private List<PeriodMetricDTO> subscriptionGrowth;
    private List<PeriodMetricDTO> revenueGrowth;
    private List<PeriodMetricDTO> nutritionPlanGrowth;

    // Construtores
    public GrowthMetricsDTO() {
    }

    public GrowthMetricsDTO(LocalDate startDate, LocalDate endDate, String period) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }

    // Getters e Setters
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<PeriodMetricDTO> getUserGrowth() {
        return userGrowth;
    }

    public void setUserGrowth(List<PeriodMetricDTO> userGrowth) {
        this.userGrowth = userGrowth;
    }

    public List<PeriodMetricDTO> getSubscriptionGrowth() {
        return subscriptionGrowth;
    }

    public void setSubscriptionGrowth(List<PeriodMetricDTO> subscriptionGrowth) {
        this.subscriptionGrowth = subscriptionGrowth;
    }

    public List<PeriodMetricDTO> getRevenueGrowth() {
        return revenueGrowth;
    }

    public void setRevenueGrowth(List<PeriodMetricDTO> revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
    }

    public List<PeriodMetricDTO> getNutritionPlanGrowth() {
        return nutritionPlanGrowth;
    }

    public void setNutritionPlanGrowth(List<PeriodMetricDTO> nutritionPlanGrowth) {
        this.nutritionPlanGrowth = nutritionPlanGrowth;
    }

    /**
     * Classe interna para métricas de um período específico
     */
    public static class PeriodMetricDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private String label; // "2024-01", "Week 1", etc
        private Long count;
        private BigDecimal value;
        private Double growthRate; // Taxa de crescimento em relação ao período anterior

        // Construtores
        public PeriodMetricDTO() {
        }

        public PeriodMetricDTO(LocalDate date, String label, Long count) {
            this.date = date;
            this.label = label;
            this.count = count;
        }

        public PeriodMetricDTO(LocalDate date, String label, BigDecimal value) {
            this.date = date;
            this.label = label;
            this.value = value;
        }

        // Getters e Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public Double getGrowthRate() {
            return growthRate;
        }

        public void setGrowthRate(Double growthRate) {
            this.growthRate = growthRate;
        }
    }
}
