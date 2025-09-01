package fiap.backend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um plano nutricional diário de um usuário
 */
@Entity
@Table(name = "nutrition_plans", indexes = {
        @Index(name = "idx_nutrition_plan_user", columnList = "user_id"),
        @Index(name = "idx_nutrition_plan_date", columnList = "plan_date"),
        @Index(name = "idx_nutrition_plan_user_date", columnList = "user_id, plan_date")
})
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "breakfast", columnDefinition = "TEXT")
    private String breakfast;

    @Column(name = "morning_snack", columnDefinition = "TEXT")
    private String morningSnack;

    @Column(name = "lunch", columnDefinition = "TEXT")
    private String lunch;

    @Column(name = "afternoon_snack", columnDefinition = "TEXT")
    private String afternoonSnack;

    @Column(name = "dinner", columnDefinition = "TEXT")
    private String dinner;

    @Column(name = "evening_snack", columnDefinition = "TEXT")
    private String eveningSnack;

    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "total_proteins")
    private Double totalProteins;

    @Column(name = "total_carbohydrates")
    private Double totalCarbohydrates;

    @Column(name = "total_fats")
    private Double totalFats;

    @Column(name = "water_intake_ml")
    private Integer waterIntakeMl;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Construtores
    public NutritionPlan() {
    }

    public NutritionPlan(User user, LocalDate planDate, String title) {
        this.user = user;
        this.planDate = planDate;
        this.title = title;
        this.isCompleted = false;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }

    public String getMorningSnack() {
        return morningSnack;
    }

    public void setMorningSnack(String morningSnack) {
        this.morningSnack = morningSnack;
    }

    public String getLunch() {
        return lunch;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public String getAfternoonSnack() {
        return afternoonSnack;
    }

    public void setAfternoonSnack(String afternoonSnack) {
        this.afternoonSnack = afternoonSnack;
    }

    public String getDinner() {
        return dinner;
    }

    public void setDinner(String dinner) {
        this.dinner = dinner;
    }

    public String getEveningSnack() {
        return eveningSnack;
    }

    public void setEveningSnack(String eveningSnack) {
        this.eveningSnack = eveningSnack;
    }

    public Integer getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(Integer totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Double getTotalProteins() {
        return totalProteins;
    }

    public void setTotalProteins(Double totalProteins) {
        this.totalProteins = totalProteins;
    }

    public Double getTotalCarbohydrates() {
        return totalCarbohydrates;
    }

    public void setTotalCarbohydrates(Double totalCarbohydrates) {
        this.totalCarbohydrates = totalCarbohydrates;
    }

    public Double getTotalFats() {
        return totalFats;
    }

    public void setTotalFats(Double totalFats) {
        this.totalFats = totalFats;
    }

    public Integer getWaterIntakeMl() {
        return waterIntakeMl;
    }

    public void setWaterIntakeMl(Integer waterIntakeMl) {
        this.waterIntakeMl = waterIntakeMl;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
        if (isCompleted && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!isCompleted) {
            this.completedAt = null;
        }
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Marca o plano como completo
     */
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marca o plano como incompleto
     */
    public void markAsIncomplete() {
        this.isCompleted = false;
        this.completedAt = null;
    }

    /**
     * Verifica se o plano está vencido (data anterior ao dia atual)
     */
    public boolean isOverdue() {
        return planDate.isBefore(LocalDate.now()) && !isCompleted;
    }

    /**
     * Verifica se o plano é de hoje
     */
    public boolean isToday() {
        return planDate.equals(LocalDate.now());
    }
}
