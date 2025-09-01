package fiap.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta para plano nutricional
 */
public class NutritionPlanResponseDTO {

    private UUID id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    private String title;
    private String description;
    private String breakfast;
    private String morningSnack;
    private String lunch;
    private String afternoonSnack;
    private String dinner;
    private String eveningSnack;
    private Integer totalCalories;
    private Double totalProteins;
    private Double totalCarbohydrates;
    private Double totalFats;
    private Integer waterIntakeMl;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Status flags para facilitar uso no frontend
    @JsonProperty("isToday")
    private Boolean isToday;

    @JsonProperty("isOverdue")
    private Boolean isOverdue;

    // Construtores
    public NutritionPlanResponseDTO() {
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Boolean getIsToday() {
        return isToday;
    }

    public void setIsToday(Boolean isToday) {
        this.isToday = isToday;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }
}
