package fiap.backend.repository;

import fiap.backend.domain.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findByName(String name);

    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = true ORDER BY sp.price ASC")
    List<SubscriptionPlan> findAllActivePlansOrderByPrice();

    List<SubscriptionPlan> findByIsActiveOrderByCreatedAtDesc(Boolean isActive);

    boolean existsByName(String name);

    // Métodos para dashboard administrativo
    List<SubscriptionPlan> findByIsActive(@Param("isActive") Boolean isActive);
}
