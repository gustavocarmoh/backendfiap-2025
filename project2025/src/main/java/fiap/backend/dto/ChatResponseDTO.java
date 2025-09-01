package fiap.backend.dto;

import fiap.backend.domain.ChatMessage;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respostas de chat
 */
public class ChatResponseDTO {

    private UUID messageId;
    private UUID sessionId;
    private String messageContent;
    private String responseContent;
    private ChatMessage.MessageType messageType;
    private ChatMessage.MessageStatus status;
    private Boolean isFromUser;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String errorMessage;

    // Construtores
    public ChatResponseDTO() {
    }

    public ChatResponseDTO(ChatMessage chatMessage) {
        this.messageId = chatMessage.getId();
        this.sessionId = chatMessage.getSessionId();
        this.messageContent = chatMessage.getMessageContent();
        this.responseContent = chatMessage.getResponseContent();
        this.messageType = chatMessage.getMessageType();
        this.status = chatMessage.getStatus();
        this.isFromUser = chatMessage.getIsFromUser();
        this.createdAt = chatMessage.getCreatedAt();
        this.processedAt = chatMessage.getProcessedAt();
        this.errorMessage = chatMessage.getErrorMessage();
    }

    // Getters e Setters
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
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

    public ChatMessage.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ChatMessage.MessageType messageType) {
        this.messageType = messageType;
    }

    public ChatMessage.MessageStatus getStatus() {
        return status;
    }

    public void setStatus(ChatMessage.MessageStatus status) {
        this.status = status;
    }

    public Boolean getIsFromUser() {
        return isFromUser;
    }

    public void setIsFromUser(Boolean isFromUser) {
        this.isFromUser = isFromUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
}
