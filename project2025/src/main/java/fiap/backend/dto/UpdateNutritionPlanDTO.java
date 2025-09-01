package fiap.backend.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para atualização de plano nutricional
 */
public class UpdateNutritionPlanDTO {

    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    private String title;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String description;

    @Size(max = 500, message = "O café da manhã deve ter no máximo 500 caracteres")
    private String breakfast;

    @Size(max = 500, message = "O lanche da manhã deve ter no máximo 500 caracteres")
    private String morningSnack;

    @Size(max = 500, message = "O almoço deve ter no máximo 500 caracteres")
    private String lunch;

    @Size(max = 500, message = "O lanche da tarde deve ter no máximo 500 caracteres")
    private String afternoonSnack;

    @Size(max = 500, message = "O jantar deve ter no máximo 500 caracteres")
    private String dinner;

    @Size(max = 500, message = "A ceia deve ter no máximo 500 caracteres")
    private String eveningSnack;

    @Min(value = 0, message = "As calorias devem ser um valor positivo")
    @Max(value = 10000, message = "As calorias não podem exceder 10.000")
    private Integer totalCalories;

    @DecimalMin(value = "0.0", message = "As proteínas devem ser um valor positivo")
    @DecimalMax(value = "1000.0", message = "As proteínas não podem exceder 1000g")
    private Double totalProteins;

    @DecimalMin(value = "0.0", message = "Os carboidratos devem ser um valor positivo")
    @DecimalMax(value = "2000.0", message = "Os carboidratos não podem exceder 2000g")
    private Double totalCarbohydrates;

    @DecimalMin(value = "0.0", message = "As gorduras devem ser um valor positivo")
    @DecimalMax(value = "500.0", message = "As gorduras não podem exceder 500g")
    private Double totalFats;

    @Min(value = 0, message = "A ingestão de água deve ser um valor positivo")
    @Max(value = 10000, message = "A ingestão de água não pode exceder 10L")
    private Integer waterIntakeMl;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres")
    private String notes;

    // Construtores
    public UpdateNutritionPlanDTO() {
    }

    // Getters e Setters
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
