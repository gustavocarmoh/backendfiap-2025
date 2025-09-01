package fiap.backend.controller;

import fiap.backend.dto.ChatHistoryDTO;
import fiap.backend.dto.ChatRequestDTO;
import fiap.backend.dto.ChatResponseDTO;
import fiap.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelas operações de chat
 */
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "API para sistema de chat")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "Enviar mensagem no chat", description = "Envia uma nova mensagem para o sistema de chat e recebe uma resposta automática")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagem enviada e processada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da mensagem inválidos"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM') or hasRole('ADMIN')")
    public ResponseEntity<ChatResponseDTO> sendMessage(
            @Valid @RequestBody ChatRequestDTO request) {

        ChatResponseDTO response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Listar sessões de chat do usuário", description = "Retorna todas as sessões de chat do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de sessões retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatHistoryDTO>> getUserSessions() {

        List<ChatHistoryDTO> sessions = chatService.getUserSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Obter histórico de uma sessão específica", description = "Retorna o histórico completo de mensagens de uma sessão de chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico da sessão retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM') or hasRole('ADMIN')")
    public ResponseEntity<ChatHistoryDTO> getSessionHistory(
            @Parameter(description = "ID da sessão de chat") @PathVariable UUID sessionId) {

        ChatHistoryDTO history = chatService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/messages")
    @Operation(summary = "Listar mensagens do usuário com paginação", description = "Retorna as mensagens do usuário autenticado com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mensagens retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM') or hasRole('ADMIN')")
    public ResponseEntity<Page<ChatResponseDTO>> getUserMessages(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<ChatResponseDTO> messages = chatService.getUserMessages(pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "Estatísticas do chat para administradores", description = "Retorna estatísticas detalhadas do uso do sistema de chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChatService.ChatStatsDTO> getChatStatistics() {

        ChatService.ChatStatsDTO stats = chatService.getChatStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/status")
    @Operation(summary = "Status do sistema de chat", description = "Retorna informações sobre o status atual do sistema de chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PREMIUM') or hasRole('ADMIN')")
    public ResponseEntity<ChatStatusDTO> getChatStatus() {

        ChatStatusDTO status = new ChatStatusDTO(
                "disponível",
                "O sistema de chat está funcionando normalmente",
                false, // IA não está disponível ainda
                false, // n8n não está integrado ainda
                "Em breve teremos integração com IA para respostas mais personalizadas!");

        return ResponseEntity.ok(status);
    }

    /**
     * DTO para status do chat
     */
    public static class ChatStatusDTO {
        private String status;
        private String message;
        private Boolean aiEnabled;
        private Boolean n8nEnabled;
        private String futureFeatures;

        public ChatStatusDTO(String status, String message, Boolean aiEnabled,
                Boolean n8nEnabled, String futureFeatures) {
            this.status = status;
            this.message = message;
            this.aiEnabled = aiEnabled;
            this.n8nEnabled = n8nEnabled;
            this.futureFeatures = futureFeatures;
        }

        // Getters
        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Boolean getAiEnabled() {
            return aiEnabled;
        }

        public Boolean getN8nEnabled() {
            return n8nEnabled;
        }

        public String getFutureFeatures() {
            return futureFeatures;
        }
    }
}
