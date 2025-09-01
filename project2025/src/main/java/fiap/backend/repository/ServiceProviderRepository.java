package fiap.backend.repository;

import fiap.backend.domain.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para operações de banco de dados com fornecedores de serviços.
 * Fornece métodos para consultas otimizadas e filtros específicos.
 */
@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, UUID> {

    /**
     * Busca todos os fornecedores ativos ordenados por nome
     * 
     * @return lista de fornecedores ativos
     */
    @Query("SELECT sp FROM ServiceProvider sp WHERE sp.active = true ORDER BY sp.name ASC")
    List<ServiceProvider> findAllActiveOrderByName();

    /**
     * Busca todos os fornecedores (ativos e inativos) ordenados por nome
     * 
     * @return lista completa de fornecedores
     */
    @Query("SELECT sp FROM ServiceProvider sp ORDER BY sp.name ASC")
    List<ServiceProvider> findAllOrderByName();

    /**
     * Busca fornecedores por nome (busca parcial e case-insensitive)
     * 
     * @param name parte do nome para buscar
     * @return lista de fornecedores que correspondem ao nome
     */
    @Query("SELECT sp FROM ServiceProvider sp WHERE LOWER(sp.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY sp.name ASC")
    List<ServiceProvider> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca fornecedores ativos por nome
     * 
     * @param name parte do nome para buscar
     * @return lista de fornecedores ativos que correspondem ao nome
     */
    @Query("SELECT sp FROM ServiceProvider sp WHERE sp.active = true AND LOWER(sp.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY sp.name ASC")
    List<ServiceProvider> findActiveByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca fornecedores dentro de uma área geográfica (bounding box)
     * 
     * @param minLat latitude mínima
     * @param maxLat latitude máxima
     * @param minLng longitude mínima
     * @param maxLng longitude máxima
     * @return lista de fornecedores na área especificada
     */
    @Query("SELECT sp FROM ServiceProvider sp WHERE sp.active = true " +
            "AND sp.latitude BETWEEN :minLat AND :maxLat " +
            "AND sp.longitude BETWEEN :minLng AND :maxLng " +
            "ORDER BY sp.name ASC")
    List<ServiceProvider> findByLocationBounds(@Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng);

    /**
     * Busca fornecedores por status ativo/inativo
     * 
     * @param active status desejado
     * @return lista de fornecedores com o status especificado
     */
    List<ServiceProvider> findByActiveOrderByName(Boolean active);

    /**
     * Verifica se existe fornecedor com o nome especificado
     * 
     * @param name nome do fornecedor
     * @return true se existe, false caso contrário
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica se existe outro fornecedor com o mesmo nome (excluindo o ID atual)
     * 
     * @param name nome do fornecedor
     * @param id   ID do fornecedor atual (para exclusão da busca)
     * @return true se existe outro fornecedor com o mesmo nome
     */
    @Query("SELECT COUNT(sp) > 0 FROM ServiceProvider sp WHERE LOWER(sp.name) = LOWER(:name) AND sp.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") UUID id);

    /**
     * Conta o número total de fornecedores ativos
     * 
     * @return número de fornecedores ativos
     */
    long countByActive(Boolean active);

    /**
     * Busca fornecedores criados por um administrador específico
     * 
     * @param adminId ID do administrador
     * @return lista de fornecedores criados pelo admin
     */
    List<ServiceProvider> findByCreatedByOrderByCreatedAtDesc(UUID adminId);

    /**
     * Busca fornecedores atualizados recentemente (útil para auditoria)
     * 
     * @return lista dos últimos fornecedores modificados
     */
    @Query("SELECT sp FROM ServiceProvider sp ORDER BY sp.updatedAt DESC")
    List<ServiceProvider> findRecentlyUpdated();

    /**
     * Busca fornecedor por ID apenas se estiver ativo
     * 
     * @param id ID do fornecedor
     * @return Optional com o fornecedor se ativo
     */
    @Query("SELECT sp FROM ServiceProvider sp WHERE sp.id = :id AND sp.active = true")
    Optional<ServiceProvider> findActiveById(@Param("id") UUID id);

    /**
     * Obtém estatísticas básicas dos fornecedores
     * 
     * @return array com [total, ativos, inativos]
     */
    @Query("SELECT COUNT(sp), " +
            "SUM(CASE WHEN sp.active = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN sp.active = false THEN 1 ELSE 0 END) " +
            "FROM ServiceProvider sp")
    Object[] getBasicStats();

    // Métodos para dashboard administrativo
    @Query("SELECT COUNT(sp) FROM ServiceProvider sp WHERE sp.active = :active")
    Long countByIsActive(@Param("active") Boolean active);

    @Query("SELECT COUNT(sp) FROM ServiceProvider sp WHERE sp.createdAt >= :start AND sp.createdAt <= :end")
    Long countByPeriod(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}