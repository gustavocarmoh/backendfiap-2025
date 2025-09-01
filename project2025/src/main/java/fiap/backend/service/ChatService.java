package fiap.backend.service;

import fiap.backend.domain.ChatMessage;
import fiap.backend.domain.User;
import fiap.backend.dto.ChatHistoryDTO;
import fiap.backend.dto.ChatRequestDTO;
import fiap.backend.dto.ChatResponseDTO;
import fiap.backend.repository.ChatMessageRepository;
import fiap.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo sistema de chat
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Processa uma nova mensagem do usuário
     */
    @Transactional
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        try {
            // Obtém o usuário autenticado
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Gera sessionId se não fornecido
            UUID sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID();

            // Cria a mensagem do usuário
            ChatMessage userMessage = new ChatMessage(
                    sessionId,
                    user,
                    request.getMessageContent(),
                    request.getMessageType());

            // Salva a mensagem do usuário
            ChatMessage savedMessage = chatMessageRepository.save(userMessage);
            logger.info("Nova mensagem de chat recebida do usuário: {} na sessão: {}",
                    user.getEmail(), sessionId);

            // Processa a resposta automática
            String automaticResponse = generateAutomaticResponse(request.getMessageType());
            savedMessage.markAsResponded(automaticResponse);

            // Atualiza com a resposta
            savedMessage = chatMessageRepository.save(savedMessage);

            return new ChatResponseDTO(savedMessage);

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem de chat: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao processar mensagem", e);
        }
    }

    /**
     * Busca o histórico de uma sessão específica
     */
    public ChatHistoryDTO getSessionHistory(UUID sessionId) {
        try {
            List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

            if (messages.isEmpty()) {
                throw new RuntimeException("Sessão não encontrada");
            }

            ChatMessage firstMessage = messages.get(0);
            User user = firstMessage.getUser();

            ChatHistoryDTO history = new ChatHistoryDTO(sessionId, user.getId(), user.getName());
            history.setSessionStarted(chatMessageRepository.findSessionStartBySessionId(sessionId));
            history.setLastActivity(chatMessageRepository.findLastActivityBySessionId(sessionId));
            history.setTotalMessages(messages.size());
            history.setMessages(messages.stream()
                    .map(ChatResponseDTO::new)
                    .collect(Collectors.toList()));

            return history;

        } catch (Exception e) {
            logger.error("Erro ao buscar histórico da sessão {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar histórico da sessão", e);
        }
    }

    /**
     * Busca todas as sessões de um usuário
     */
    public List<ChatHistoryDTO> getUserSessions() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<UUID> sessionIds = chatMessageRepository.findDistinctSessionIdsByUser(user);

            return sessionIds.stream()
                    .map(sessionId -> {
                        ChatHistoryDTO history = new ChatHistoryDTO(sessionId, user.getId(), user.getName());
                        history.setSessionStarted(chatMessageRepository.findSessionStartBySessionId(sessionId));
                        history.setLastActivity(chatMessageRepository.findLastActivityBySessionId(sessionId));
                        history.setTotalMessages(Math.toIntExact(chatMessageRepository.countBySessionId(sessionId)));
                        // Não carrega as mensagens completas para performance
                        return history;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Erro ao buscar sessões do usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar sessões do usuário", e);
        }
    }

    /**
     * Busca mensagens de um usuário com paginação
     */
    public Page<ChatResponseDTO> getUserMessages(Pageable pageable) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Page<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            return messages.map(ChatResponseDTO::new);

        } catch (Exception e) {
            logger.error("Erro ao buscar mensagens do usuário: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar mensagens do usuário", e);
        }
    }

    /**
     * Gera resposta automática baseada no tipo da mensagem
     * No futuro, esta funcionalidade será substituída por IA ou integração com n8n
     */
    private String generateAutomaticResponse(ChatMessage.MessageType messageType) {
        String baseMessage = "Olá! Ficamos felizes em saber que você gostaria de ter informações mais detalhadas e personalizadas. ";

        switch (messageType) {
            case NUTRITION_QUESTION:
                return baseMessage +
                        "Sua pergunta sobre nutrição é muito importante para nós. " +
                        "No momento, nosso chat inteligente não está disponível, mas em breve teremos " +
                        "respostas personalizadas sobre planos nutricionais, dicas de alimentação e muito mais!";

            case SUBSCRIPTION_QUESTION:
                return baseMessage +
                        "Entendemos sua dúvida sobre assinatura. " +
                        "No momento, nosso chat não está disponível, mas em breve você poderá " +
                        "tirar todas as dúvidas sobre planos, benefícios e formas de pagamento!";

            case TECHNICAL_SUPPORT:
                return baseMessage +
                        "Sabemos que questões técnicas podem ser urgentes. " +
                        "No momento, nosso chat não está disponível, mas em breve teremos " +
                        "suporte técnico automatizado para resolver seus problemas rapidamente!";

            case FEATURE_REQUEST:
                return baseMessage +
                        "Adoramos receber sugestões de melhorias! " +
                        "No momento, nosso chat não está disponível, mas em breve você poderá " +
                        "sugerir novas funcionalidades e acompanhar o desenvolvimento delas!";

            case COMPLAINT:
                return baseMessage +
                        "Lamentamos que tenha tido uma experiência negativa. " +
                        "No momento, nosso chat não está disponível, mas em breve teremos " +
                        "um atendimento personalizado para resolver rapidamente qualquer problema!";

            case COMPLIMENT:
                return baseMessage +
                        "Que bom saber que está satisfeito com nossos serviços! " +
                        "No momento, nosso chat não está disponível, mas em breve você poderá " +
                        "compartilhar mais feedback e sugestões conosco!";

            default:
                return baseMessage +
                        "No momento, nosso chat inteligente não está disponível, mas estamos trabalhando " +
                        "para implementar um sistema completo de atendimento com inteligência artificial " +
                        "que poderá responder suas perguntas de forma personalizada e eficiente!";
        }
    }

    /**
     * Estatísticas do chat para admins
     */
    public ChatStatsDTO getChatStatistics() {
        try {
            Long totalMessages = chatMessageRepository.count();
            Long totalSessions = chatMessageRepository.countDistinctSessions();
            Long totalUsers = chatMessageRepository.countDistinctUsers();

            List<Object[]> messagesByStatus = chatMessageRepository.countMessagesByStatus();
            List<Object[]> messagesByType = chatMessageRepository.countMessagesByType();

            // Sessões ativas nas últimas 24 horas
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            List<UUID> activeSessions = chatMessageRepository.findActiveSessionsSince(last24Hours);

            return new ChatStatsDTO(
                    totalMessages,
                    totalSessions,
                    totalUsers,
                    activeSessions.size(),
                    messagesByStatus,
                    messagesByType);

        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas do chat: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar estatísticas do chat", e);
        }
    }

    /**
     * DTO interno para estatísticas do chat
     */
    public static class ChatStatsDTO {
        private Long totalMessages;
        private Long totalSessions;
        private Long totalUsers;
        private Integer activeSessionsLast24h;
        private List<Object[]> messagesByStatus;
        private List<Object[]> messagesByType;

        public ChatStatsDTO(Long totalMessages, Long totalSessions, Long totalUsers,
                Integer activeSessionsLast24h, List<Object[]> messagesByStatus,
                List<Object[]> messagesByType) {
            this.totalMessages = totalMessages;
            this.totalSessions = totalSessions;
            this.totalUsers = totalUsers;
            this.activeSessionsLast24h = activeSessionsLast24h;
            this.messagesByStatus = messagesByStatus;
            this.messagesByType = messagesByType;
        }

        // Getters
        public Long getTotalMessages() {
            return totalMessages;
        }

        public Long getTotalSessions() {
            return totalSessions;
        }

        public Long getTotalUsers() {
            return totalUsers;
        }

        public Integer getActiveSessionsLast24h() {
            return activeSessionsLast24h;
        }

        public List<Object[]> getMessagesByStatus() {
            return messagesByStatus;
        }

        public List<Object[]> getMessagesByType() {
            return messagesByType;
        }
    }
}
