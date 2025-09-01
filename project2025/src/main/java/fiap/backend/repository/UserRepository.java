package fiap.backend.repository;

import fiap.backend.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, java.util.UUID> {
    // Métodos customizados se necessário
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Métodos para dashboard administrativo
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    Long countActiveUsers(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt <= :end")
    Long countUsersByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = :date")
    Long countUsersByDate(@Param("date") LocalDate date);
}
