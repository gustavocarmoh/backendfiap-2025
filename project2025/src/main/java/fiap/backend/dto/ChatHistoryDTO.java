package fiap.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para histórico de conversas de chat
 */
public class ChatHistoryDTO {

    private UUID sessionId;
    private UUID userId;
    private String userName;
    private LocalDateTime sessionStarted;
    private LocalDateTime lastActivity;
    private Integer totalMessages;
    private List<ChatResponseDTO> messages;
    private Boolean isActive;

    // Construtores
    public ChatHistoryDTO() {
    }

    public ChatHistoryDTO(UUID sessionId, UUID userId, String userName) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userName = userName;
        this.isActive = true;
    }

    // Getters e Setters
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getSessionStarted() {
        return sessionStarted;
    }

    public void setSessionStarted(LocalDateTime sessionStarted) {
        this.sessionStarted = sessionStarted;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Integer getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(Integer totalMessages) {
        this.totalMessages = totalMessages;
    }

    public List<ChatResponseDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatResponseDTO> messages) {
        this.messages = messages;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
