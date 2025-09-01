package fiap.backend.dto;

import fiap.backend.domain.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO para requisições de chat
 */
public class ChatRequestDTO {

    @NotBlank(message = "O conteúdo da mensagem é obrigatório")
    @Size(max = 2000, message = "A mensagem não pode exceder 2000 caracteres")
    private String messageContent;

    @NotNull(message = "O tipo da mensagem é obrigatório")
    private ChatMessage.MessageType messageType;

    private UUID sessionId; // Opcional - será gerado se não fornecido

    // Construtores
    public ChatRequestDTO() {
    }

    public ChatRequestDTO(String messageContent, ChatMessage.MessageType messageType) {
        this.messageContent = messageContent;
        this.messageType = messageType;
    }

    public ChatRequestDTO(String messageContent, ChatMessage.MessageType messageType, UUID sessionId) {
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.sessionId = sessionId;
    }

    // Getters e Setters
    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public ChatMessage.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ChatMessage.MessageType messageType) {
        this.messageType = messageType;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}
