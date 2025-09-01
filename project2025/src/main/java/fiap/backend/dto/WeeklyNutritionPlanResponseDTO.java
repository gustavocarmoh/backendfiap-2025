package fiap.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de resposta para visualização semanal de planos nutricionais
 */
public class WeeklyNutritionPlanResponseDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd;

    private List<NutritionPlanResponseDTO> plans;

    // Estatísticas da semana
    private WeeklyStatsDTO weeklyStats;

    // Construtores
    public WeeklyNutritionPlanResponseDTO() {
    }

    public WeeklyNutritionPlanResponseDTO(LocalDate weekStart, LocalDate weekEnd,
            List<NutritionPlanResponseDTO> plans) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.plans = plans;
    }

    // Getters e Setters
    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public List<NutritionPlanResponseDTO> getPlans() {
        return plans;
    }

    public void setPlans(List<NutritionPlanResponseDTO> plans) {
        this.plans = plans;
    }

    public WeeklyStatsDTO getWeeklyStats() {
        return weeklyStats;
    }

    public void setWeeklyStats(WeeklyStatsDTO weeklyStats) {
        this.weeklyStats = weeklyStats;
    }

    /**
     * Classe interna para estatísticas semanais
     */
    public static class WeeklyStatsDTO {
        private Integer totalPlans;
        private Integer completedPlans;
        private Integer pendingPlans;

        @JsonProperty("completionRate")
        private Double completionRate; // Percentual de conclusão

        private Double averageCalories;
        private Double totalWaterIntake;

        // Construtores
        public WeeklyStatsDTO() {
        }

        // Getters e Setters
        public Integer getTotalPlans() {
            return totalPlans;
        }

        public void setTotalPlans(Integer totalPlans) {
            this.totalPlans = totalPlans;
        }

        public Integer getCompletedPlans() {
            return completedPlans;
        }

        public void setCompletedPlans(Integer completedPlans) {
            this.completedPlans = completedPlans;
        }

        public Integer getPendingPlans() {
            return pendingPlans;
        }

        public void setPendingPlans(Integer pendingPlans) {
            this.pendingPlans = pendingPlans;
        }

        public Double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(Double completionRate) {
            this.completionRate = completionRate;
        }

        public Double getAverageCalories() {
            return averageCalories;
        }

        public void setAverageCalories(Double averageCalories) {
            this.averageCalories = averageCalories;
        }

        public Double getTotalWaterIntake() {
            return totalWaterIntake;
        }

        public void setTotalWaterIntake(Double totalWaterIntake) {
            this.totalWaterIntake = totalWaterIntake;
        }
    }
}
