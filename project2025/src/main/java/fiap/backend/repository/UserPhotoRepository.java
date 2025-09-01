package fiap.backend.repository;

import fiap.backend.domain.UserPhoto;
import fiap.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de banco de dados relacionadas às fotos de
 * usuário.
 * Fornece métodos para buscar, salvar e gerenciar fotos de perfil.
 */
@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, UUID> {

    /**
     * Busca a foto de perfil de um usuário específico
     * 
     * @param user entidade User
     * @return Optional contendo a foto se encontrada
     */
    Optional<UserPhoto> findByUser(User user);

    /**
     * Busca a foto de perfil pelo ID do usuário
     * 
     * @param userId UUID do usuário
     * @return Optional contendo a foto se encontrada
     */
    @Query("SELECT up FROM UserPhoto up WHERE up.user.id = :userId")
    Optional<UserPhoto> findByUserId(@Param("userId") UUID userId);

    /**
     * Verifica se um usuário possui foto de perfil
     * 
     * @param user entidade User
     * @return true se o usuário possui foto
     */
    boolean existsByUser(User user);

    /**
     * Verifica se um usuário possui foto de perfil pelo ID
     * 
     * @param userId UUID do usuário
     * @return true se o usuário possui foto
     */
    @Query("SELECT COUNT(up) > 0 FROM UserPhoto up WHERE up.user.id = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);

    /**
     * Remove a foto de perfil de um usuário
     * 
     * @param user entidade User
     */
    void deleteByUser(User user);

    /**
     * Remove a foto de perfil pelo ID do usuário
     * 
     * @param userId UUID do usuário
     */
    @Query("DELETE FROM UserPhoto up WHERE up.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Busca apenas os metadados da foto (sem os dados binários) para otimização
     * 
     * @param userId UUID do usuário
     * @return Optional com metadados da foto
     */
    @Query("SELECT new fiap.backend.domain.UserPhoto(up.user, null, up.fileName, up.contentType) " +
            "FROM UserPhoto up WHERE up.user.id = :userId")
    Optional<UserPhoto> findMetadataByUserId(@Param("userId") UUID userId);

    /**
     * Conta o número total de fotos armazenadas no sistema
     * 
     * @return número total de fotos
     */
    @Query("SELECT COUNT(up) FROM UserPhoto up")
    long countAllPhotos();

    /**
     * Calcula o tamanho total ocupado pelas fotos em bytes
     * 
     * @return tamanho total em bytes
     */
    @Query("SELECT COALESCE(SUM(up.fileSize), 0) FROM UserPhoto up")
    long getTotalStorageUsed();

    // Métodos para dashboard administrativo
    @Query("SELECT COUNT(up) FROM UserPhoto up WHERE up.createdAt >= :start AND up.createdAt <= :end")
    Long countByPeriod(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
