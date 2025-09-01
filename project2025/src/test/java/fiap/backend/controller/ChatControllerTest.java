package fiap.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.backend.domain.ChatMessage;
import fiap.backend.dto.ChatHistoryDTO;
import fiap.backend.dto.ChatRequestDTO;
import fiap.backend.dto.ChatResponseDTO;
import fiap.backend.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatRequestDTO testRequest;
    private ChatResponseDTO testResponse;
    private ChatHistoryDTO testHistory;
    private UUID testSessionId;

    @BeforeEach
    void setUp() {
        testSessionId = UUID.randomUUID();

        testRequest = new ChatRequestDTO();
        testRequest.setMessageContent("Como posso melhorar minha alimentação?");
        testRequest.setMessageType(ChatMessage.MessageType.NUTRITION_QUESTION);
        testRequest.setSessionId(testSessionId);

        testResponse = new ChatResponseDTO();
        testResponse.setMessageId(UUID.randomUUID());
        testResponse.setSessionId(testSessionId);
        testResponse.setMessageContent("Como posso melhorar minha alimentação?");
        testResponse.setResponseContent(
                "Ficamos felizes em saber que você gostaria de ter informações mais detalhadas sobre nutrição...");
        testResponse.setMessageType(ChatMessage.MessageType.NUTRITION_QUESTION);
        testResponse.setStatus(ChatMessage.MessageStatus.RESPONDED);
        testResponse.setIsFromUser(true);
        testResponse.setCreatedAt(LocalDateTime.now());
        testResponse.setProcessedAt(LocalDateTime.now());

        testHistory = new ChatHistoryDTO();
        testHistory.setSessionId(testSessionId);
        testHistory.setUserId(UUID.randomUUID());
        testHistory.setUserName("João Silva");
        testHistory.setSessionStarted(LocalDateTime.now().minusHours(1));
        testHistory.setLastActivity(LocalDateTime.now());
        testHistory.setTotalMessages(3);
        testHistory.setMessages(Arrays.asList(testResponse));
        testHistory.setIsActive(true);
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendMessage_DeveEnviarMensagemComSucesso() throws Exception {
        // Arrange
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(testResponse.getMessageId().toString()))
                .andExpect(jsonPath("$.sessionId").value(testSessionId.toString()))
                .andExpect(jsonPath("$.messageContent").value("Como posso melhorar minha alimentação?"))
                .andExpect(jsonPath("$.responseContent").exists())
                .andExpect(jsonPath("$.messageType").value("NUTRITION_QUESTION"))
                .andExpect(jsonPath("$.status").value("RESPONDED"))
                .andExpect(jsonPath("$.isFromUser").value(true));
    }

    @Test
    @WithMockUser(roles = "PREMIUM")
    void sendMessage_DevePermitirUsuarioPremium() throws Exception {
        // Arrange
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendMessage_DevePermitirAdmin() throws Exception {
        // Arrange
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void sendMessage_DeveRejeitarUsuarioNaoAutenticado() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendMessage_DeveValidarMensagemObrigatoria() throws Exception {
        // Arrange
        testRequest.setMessageContent("");

        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendMessage_DeveValidarTipoMensagemObrigatorio() throws Exception {
        // Arrange
        testRequest.setMessageType(null);

        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserSessions_DeveRetornarSessoesDoUsuario() throws Exception {
        // Arrange
        List<ChatHistoryDTO> sessions = Arrays.asList(testHistory);
        when(chatService.getUserSessions()).thenReturn(sessions);

        // Act & Assert
        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sessionId").value(testSessionId.toString()))
                .andExpect(jsonPath("$[0].userName").value("João Silva"))
                .andExpect(jsonPath("$[0].totalMessages").value(3))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSessionHistory_DeveRetornarHistoricoDaSessao() throws Exception {
        // Arrange
        when(chatService.getSessionHistory(testSessionId)).thenReturn(testHistory);

        // Act & Assert
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", testSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(testSessionId.toString()))
                .andExpect(jsonPath("$.userName").value("João Silva"))
                .andExpect(jsonPath("$.totalMessages").value(3))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages[0].messageContent").value("Como posso melhorar minha alimentação?"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserMessages_DeveRetornarMensagensComPaginacao() throws Exception {
        // Arrange
        Page<ChatResponseDTO> messagesPage = new PageImpl<>(
                Arrays.asList(testResponse),
                PageRequest.of(0, 20),
                1);
        when(chatService.getUserMessages(any())).thenReturn(messagesPage);

        // Act & Assert
        mockMvc.perform(get("/api/chat/messages")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].messageContent").value("Como posso melhorar minha alimentação?"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getChatStatistics_DeveRetornarEstatisticasParaAdmin() throws Exception {
        // Arrange
        List<Object[]> messagesByStatus = Collections.singletonList(
                new Object[] { "RESPONDED", 80L });
        List<Object[]> messagesByType = Collections.singletonList(
                new Object[] { "NUTRITION_QUESTION", 40L });

        ChatService.ChatStatsDTO stats = new ChatService.ChatStatsDTO(
                100L, 25L, 15L, 5, messagesByStatus, messagesByType);
        when(chatService.getChatStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/chat/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(100))
                .andExpect(jsonPath("$.totalSessions").value(25))
                .andExpect(jsonPath("$.totalUsers").value(15))
                .andExpect(jsonPath("$.activeSessionsLast24h").value(5));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getChatStatistics_DeveRejeitarUsuarioComum() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/chat/admin/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getChatStatus_DeveRetornarStatusDoChat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("disponível"))
                .andExpect(jsonPath("$.message").value("O sistema de chat está funcionando normalmente"))
                .andExpect(jsonPath("$.aiEnabled").value(false))
                .andExpect(jsonPath("$.n8nEnabled").value(false))
                .andExpect(jsonPath("$.futureFeatures").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendMessage_DeveValidarTamanhoMaximoMensagem() throws Exception {
        // Arrange
        String mensagemMuitoGrande = "A".repeat(2001); // Excede o limite de 2000 caracteres
        testRequest.setMessageContent(mensagemMuitoGrande);

        // Act & Assert
        mockMvc.perform(post("/api/chat/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void sendMessage_DeveAceitarTodosOsTiposDeMensagem() throws Exception {
        // Arrange
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(testResponse);

        for (ChatMessage.MessageType tipo : ChatMessage.MessageType.values()) {
            testRequest.setMessageType(tipo);
            testResponse.setMessageType(tipo);

            // Act & Assert
            mockMvc.perform(post("/api/chat/send")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.messageType").value(tipo.name()));
        }
    }
}
