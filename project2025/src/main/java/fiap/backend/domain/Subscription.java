package fiap.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @NotNull
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    @Column(name = "approved_by_user_id")
    private UUID approvedByUserId;

    @Column(name = "subscription_date")
    private LocalDateTime subscriptionDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    public enum SubscriptionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    // Constructors
    public Subscription() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.subscriptionDate = LocalDateTime.now();
    }

    public Subscription(User user, SubscriptionPlan plan) {
        this();
        this.user = user;
        this.plan = plan;
        this.amount = plan.getPrice();
    }

    // Getters and Setters
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

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
        if (plan != null) {
            this.amount = plan.getPrice();
        }
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public UUID getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(UUID approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }

    public LocalDateTime getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(LocalDateTime subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Business methods
    public void approve(UUID adminUserId) {
        this.status = SubscriptionStatus.APPROVED;
        this.approvedByUserId = adminUserId;
        this.approvedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(UUID adminUserId) {
        this.status = SubscriptionStatus.REJECTED;
        this.approvedByUserId = adminUserId;
        this.approvedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == SubscriptionStatus.PENDING;
    }

    public boolean isApproved() {
        return status == SubscriptionStatus.APPROVED;
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
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", planId=" + (plan != null ? plan.getId() : null) +
                ", status=" + status +
                ", amount=" + amount +
                '}';
    }
}
