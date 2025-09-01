package fiap.backend.repository;

import fiap.backend.domain.Subscription;
import fiap.backend.domain.Subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Subscription> findByStatusOrderByCreatedAtDesc(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = :status")
    List<Subscription> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'APPROVED'")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'PENDING' ORDER BY s.createdAt ASC")
    List<Subscription> findPendingSubscriptions();

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.user.id = :userId AND s.status = 'APPROVED'")
    long countActiveSubscriptionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user JOIN FETCH s.plan WHERE s.id = :id")
    Optional<Subscription> findByIdWithUserAndPlan(@Param("id") UUID id);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user JOIN FETCH s.plan ORDER BY s.createdAt DESC")
    List<Subscription> findAllWithUserAndPlan();

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user JOIN FETCH s.plan WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Subscription> findByStatusWithUserAndPlan(@Param("status") SubscriptionStatus status);

    // Métodos para dashboard administrativo
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    Long countByStatus(@Param("status") SubscriptionStatus status);

    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = 'APPROVED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = 'APPROVED' AND s.approvedDate >= :since")
    BigDecimal calculateMonthlyRevenue(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan.id = :planId AND s.status = 'APPROVED'")
    Long countActiveSubscriptionsByPlan(@Param("planId") UUID planId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE DATE(s.createdAt) = :date")
    Long countSubscriptionsByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE DATE(s.approvedDate) = :date AND s.status = 'APPROVED'")
    BigDecimal calculateDailyRevenue(@Param("date") LocalDate date);
}
