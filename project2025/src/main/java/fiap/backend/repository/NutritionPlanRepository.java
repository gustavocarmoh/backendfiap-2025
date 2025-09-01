package fiap.backend.repository;

import fiap.backend.domain.NutritionPlan;
import fiap.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações com planos nutricionais
 */
@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, UUID> {

    /**
     * Busca plano nutricional de um usuário por data específica
     */
    Optional<NutritionPlan> findByUserAndPlanDate(User user, LocalDate planDate);

    /**
     * Busca planos nutricionais de um usuário em um período
     */
    List<NutritionPlan> findByUserAndPlanDateBetweenOrderByPlanDate(
            User user, LocalDate startDate, LocalDate endDate);

    /**
     * Busca planos nutricionais de um usuário com paginação
     */
    Page<NutritionPlan> findByUserOrderByPlanDateDesc(User user, Pageable pageable);

    /**
     * Busca planos nutricionais de hoje para um usuário
     */
    @Query("SELECT np FROM NutritionPlan np WHERE np.user = :user AND np.planDate = :today")
    Optional<NutritionPlan> findTodayPlan(@Param("user") User user, @Param("today") LocalDate today);

    /**
     * Busca planos nutricionais da semana atual para um usuário
     */
    @Query("SELECT np FROM NutritionPlan np WHERE np.user = :user " +
            "AND np.planDate >= :weekStart AND np.planDate <= :weekEnd " +
            "ORDER BY np.planDate")
    List<NutritionPlan> findWeekPlans(@Param("user") User user,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);

    /**
     * Conta quantos planos nutricionais um usuário possui
     */
    long countByUser(User user);

    /**
     * Conta quantos planos nutricionais completos um usuário possui
     */
    long countByUserAndIsCompleted(User user, Boolean isCompleted);

    /**
     * Busca planos nutricionais completos de um usuário em um período
     */
    @Query("SELECT np FROM NutritionPlan np WHERE np.user = :user " +
            "AND np.isCompleted = true " +
            "AND np.planDate >= :startDate AND np.planDate <= :endDate " +
            "ORDER BY np.planDate DESC")
    List<NutritionPlan> findCompletedPlansByPeriod(@Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Busca planos nutricionais pendentes (não completos) de um usuário
     */
    @Query("SELECT np FROM NutritionPlan np WHERE np.user = :user " +
            "AND np.isCompleted = false " +
            "AND np.planDate <= :currentDate " +
            "ORDER BY np.planDate")
    List<NutritionPlan> findPendingPlans(@Param("user") User user,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Busca planos nutricionais futuros de um usuário
     */
    @Query("SELECT np FROM NutritionPlan np WHERE np.user = :user " +
            "AND np.planDate > :currentDate " +
            "ORDER BY np.planDate")
    List<NutritionPlan> findFuturePlans(@Param("user") User user,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Verifica se já existe um plano para um usuário em uma data específica
     */
    boolean existsByUserAndPlanDate(User user, LocalDate planDate);

    /**
     * Busca planos nutricionais com consumo de água registrado
     */
    @Query("SELECT np FROM NutritionPlan np WHERE np.user = :user " +
            "AND np.waterIntakeMl IS NOT NULL " +
            "AND np.planDate >= :startDate AND np.planDate <= :endDate " +
            "ORDER BY np.planDate")
    List<NutritionPlan> findPlansWithWaterIntake(@Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calcula a média de calorias consumidas por um usuário em um período
     */
    @Query("SELECT AVG(np.totalCalories) FROM NutritionPlan np WHERE np.user = :user " +
            "AND np.totalCalories IS NOT NULL " +
            "AND np.planDate >= :startDate AND np.planDate <= :endDate")
    Double calculateAverageCalories(@Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Métodos para dashboard administrativo
    @Query("SELECT COUNT(np) FROM NutritionPlan np WHERE np.isCompleted = :isCompleted")
    Long countByIsCompleted(@Param("isCompleted") Boolean isCompleted);

    @Query("SELECT COUNT(np) FROM NutritionPlan np WHERE np.planDate >= :startDate AND np.planDate <= :endDate")
    Long countByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(np) FROM NutritionPlan np WHERE DATE(np.createdAt) = :date")
    Long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(np) FROM NutritionPlan np JOIN np.user u JOIN Subscription s ON s.user = u " +
            "WHERE s.plan.id = :planId AND s.status = 'APPROVED'")
    Long countBySubscriptionPlan(@Param("planId") UUID planId);
}