package fiap.backend.repository;

import fiap.backend.domain.ChatMessage;
import fiap.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Busca todas as mensagens de uma sessão específica ordenadas por data de
     * criação
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sessionId = :sessionId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") UUID sessionId);

    /**
     * Busca todas as mensagens de um usuário específico
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user = :user ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    /**
     * Busca mensagens por status
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.status = :status ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByStatusOrderByCreatedAtAsc(@Param("status") ChatMessage.MessageStatus status);

    /**
     * Busca mensagens pendentes para processamento
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.status = 'PENDING' ORDER BY cm.createdAt ASC")
    List<ChatMessage> findPendingMessages();

    /**
     * Busca sessões únicas de um usuário
     */
    @Query("SELECT DISTINCT cm.sessionId FROM ChatMessage cm WHERE cm.user = :user ORDER BY MAX(cm.createdAt) DESC")
    List<UUID> findDistinctSessionIdsByUser(@Param("user") User user);

    /**
     * Conta total de mensagens de uma sessão
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.sessionId = :sessionId")
    Long countBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * Busca última atividade de uma sessão
     */
    @Query("SELECT MAX(cm.createdAt) FROM ChatMessage cm WHERE cm.sessionId = :sessionId")
    LocalDateTime findLastActivityBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * Busca primeira mensagem de uma sessão (início da sessão)
     */
    @Query("SELECT MIN(cm.createdAt) FROM ChatMessage cm WHERE cm.sessionId = :sessionId")
    LocalDateTime findSessionStartBySessionId(@Param("sessionId") UUID sessionId);

    /**
     * Busca mensagens de um período específico
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.createdAt BETWEEN :startDate AND :endDate ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Busca mensagens por tipo
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.messageType = :messageType ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByMessageTypeOrderByCreatedAtDesc(@Param("messageType") ChatMessage.MessageType messageType);

    /**
     * Estatísticas de mensagens por status
     */
    @Query("SELECT cm.status, COUNT(cm) FROM ChatMessage cm GROUP BY cm.status")
    List<Object[]> countMessagesByStatus();

    /**
     * Estatísticas de mensagens por tipo
     */
    @Query("SELECT cm.messageType, COUNT(cm) FROM ChatMessage cm GROUP BY cm.messageType")
    List<Object[]> countMessagesByType();

    /**
     * Busca sessões ativas (com atividade recente)
     */
    @Query("SELECT DISTINCT cm.sessionId FROM ChatMessage cm WHERE cm.createdAt >= :since")
    List<UUID> findActiveSessionsSince(@Param("since") LocalDateTime since);

    /**
     * Conta total de sessões únicas
     */
    @Query("SELECT COUNT(DISTINCT cm.sessionId) FROM ChatMessage cm")
    Long countDistinctSessions();

    /**
     * Conta total de usuários únicos que usaram o chat
     */
    @Query("SELECT COUNT(DISTINCT cm.user) FROM ChatMessage cm")
    Long countDistinctUsers();
}
