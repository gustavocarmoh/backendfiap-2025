package fiap.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotNull
    @Positive
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "nutrition_plans_limit")
    private Integer nutritionPlansLimit; // null = ilimitado, número = limite

    @Column(name = "description")
    private String description;

    // Constructors
    public SubscriptionPlan() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SubscriptionPlan(String name, BigDecimal price) {
        this();
        this.name = name;
        this.price = price;
    }

    public SubscriptionPlan(String name, BigDecimal price, Integer nutritionPlansLimit, String description) {
        this();
        this.name = name;
        this.price = price;
        this.nutritionPlansLimit = nutritionPlansLimit;
        this.description = description;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Obtém o limite de planos nutricionais
     * 
     * @return limite de planos (null = ilimitado)
     */
    public Integer getNutritionPlansLimit() {
        return nutritionPlansLimit;
    }

    public void setNutritionPlansLimit(Integer nutritionPlansLimit) {
        this.nutritionPlansLimit = nutritionPlansLimit;
    }

    /**
     * Verifica se o plano oferece planos nutricionais ilimitados
     * 
     * @return true se ilimitado
     */
    public boolean hasUnlimitedNutritionPlans() {
        return nutritionPlansLimit == null;
    }

    /**
     * Obtém a descrição do plano
     * 
     * @return descrição do plano
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SubscriptionPlan that = (SubscriptionPlan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", isActive=" + isActive +
                '}';
    }
}
