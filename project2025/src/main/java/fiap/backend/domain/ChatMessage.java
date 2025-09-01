package fiap.backend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma mensagem de chat
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_session", columnList = "session_id"),
        @Index(name = "idx_chat_user", columnList = "user_id"),
        @Index(name = "idx_chat_timestamp", columnList = "created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "is_from_user", nullable = false)
    private Boolean isFromUser = true;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum MessageType {
        GENERAL_QUESTION,
        NUTRITION_QUESTION,
        SUBSCRIPTION_QUESTION,
        TECHNICAL_SUPPORT,
        FEATURE_REQUEST,
        COMPLAINT,
        COMPLIMENT,
        OTHER
    }

    public enum MessageStatus {
        PENDING,
        PROCESSING,
        RESPONDED,
        ERROR,
        IGNORED
    }

    // Construtores
    public ChatMessage() {
    }

    public ChatMessage(UUID sessionId, User user, String messageContent, MessageType messageType) {
        this.sessionId = sessionId;
        this.user = user;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.isFromUser = true;
        this.status = MessageStatus.PENDING;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Boolean getIsFromUser() {
        return isFromUser;
    }

    public void setIsFromUser(Boolean isFromUser) {
        this.isFromUser = isFromUser;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    /**
     * Marca a mensagem como processada com resposta
     */
    public void markAsResponded(String response) {
        this.responseContent = response;
        this.status = MessageStatus.RESPONDED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marca a mensagem como erro
     */
    public void markAsError(String errorMessage) {
        this.status = MessageStatus.ERROR;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marca a mensagem como sendo processada
     */
    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
    }
}
