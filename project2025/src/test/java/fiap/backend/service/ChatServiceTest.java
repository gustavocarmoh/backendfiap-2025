package fiap.backend.service;

import fiap.backend.domain.ChatMessage;
import fiap.backend.domain.User;
import fiap.backend.dto.ChatHistoryDTO;
import fiap.backend.dto.ChatRequestDTO;
import fiap.backend.dto.ChatResponseDTO;
import fiap.backend.repository.ChatMessageRepository;
import fiap.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private ChatMessage testMessage;
    private UUID testSessionId;
    private ChatRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // Setup do usuário de teste
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("João");
        testUser.setLastName("Silva");

        // Setup da sessão de teste
        testSessionId = UUID.randomUUID();

        // Setup da mensagem de teste
        testMessage = new ChatMessage();
        testMessage.setId(UUID.randomUUID());
        testMessage.setSessionId(testSessionId);
        testMessage.setUser(testUser);
        testMessage.setMessageContent("Olá, tenho uma dúvida sobre nutrição");
        testMessage.setMessageType(ChatMessage.MessageType.NUTRITION_QUESTION);
        testMessage.setStatus(ChatMessage.MessageStatus.PENDING);
        testMessage.setIsFromUser(true);
        testMessage.setCreatedAt(LocalDateTime.now());

        // Setup da requisição de teste
        testRequest = new ChatRequestDTO();
        testRequest.setMessageContent("Olá, tenho uma dúvida sobre nutrição");
        testRequest.setMessageType(ChatMessage.MessageType.NUTRITION_QUESTION);
        testRequest.setSessionId(testSessionId);

        // Mock do contexto de segurança
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    void sendMessage_DeveCriarEProcessarMensagemComSucesso() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // Act
        ChatResponseDTO response = chatService.sendMessage(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testMessage.getId(), response.getMessageId());
        assertEquals(testSessionId, response.getSessionId());
        assertEquals("Olá, tenho uma dúvida sobre nutrição", response.getMessageContent());
        assertNotNull(response.getResponseContent());
        assertTrue(response.getResponseContent().contains("nutrição"));
        assertEquals(ChatMessage.MessageType.NUTRITION_QUESTION, response.getMessageType());

        verify(userRepository).findByEmail("test@example.com");
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_DeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> chatService.sendMessage(testRequest));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_DeveGerarSessionIdQuandoNaoFornecido() {
        // Arrange
        testRequest.setSessionId(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // Act
        ChatResponseDTO response = chatService.sendMessage(testRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSessionId());

        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void getSessionHistory_DeveRetornarHistoricoComSucesso() {
        // Arrange
        List<ChatMessage> messages = Arrays.asList(testMessage);
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(testSessionId))
                .thenReturn(messages);
        when(chatMessageRepository.findSessionStartBySessionId(testSessionId))
                .thenReturn(LocalDateTime.now().minusHours(1));
        when(chatMessageRepository.findLastActivityBySessionId(testSessionId))
                .thenReturn(LocalDateTime.now());

        // Act
        ChatHistoryDTO history = chatService.getSessionHistory(testSessionId);

        // Assert
        assertNotNull(history);
        assertEquals(testSessionId, history.getSessionId());
        assertEquals(testUser.getId(), history.getUserId());
        assertEquals("João Silva", history.getUserName());
        assertEquals(1, history.getTotalMessages());
        assertNotNull(history.getMessages());
        assertEquals(1, history.getMessages().size());

        verify(chatMessageRepository).findBySessionIdOrderByCreatedAtAsc(testSessionId);
    }

    @Test
    void getSessionHistory_DeveLancarExcecaoQuandoSessaoNaoEncontrada() {
        // Arrange
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(testSessionId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> chatService.getSessionHistory(testSessionId));

        assertEquals("Sessão não encontrada", exception.getMessage());
    }

    @Test
    void getUserSessions_DeveRetornarSessoesDoUsuario() {
        // Arrange
        List<UUID> sessionIds = Arrays.asList(testSessionId, UUID.randomUUID());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findDistinctSessionIdsByUser(testUser)).thenReturn(sessionIds);
        when(chatMessageRepository.findSessionStartBySessionId(any(UUID.class)))
                .thenReturn(LocalDateTime.now().minusHours(2));
        when(chatMessageRepository.findLastActivityBySessionId(any(UUID.class)))
                .thenReturn(LocalDateTime.now());
        when(chatMessageRepository.countBySessionId(any(UUID.class))).thenReturn(5L);

        // Act
        List<ChatHistoryDTO> sessions = chatService.getUserSessions();

        // Assert
        assertNotNull(sessions);
        assertEquals(2, sessions.size());

        ChatHistoryDTO firstSession = sessions.get(0);
        assertEquals(testUser.getId(), firstSession.getUserId());
        assertEquals("João Silva", firstSession.getUserName());
        assertEquals(5, firstSession.getTotalMessages());

        verify(userRepository).findByEmail("test@example.com");
        verify(chatMessageRepository).findDistinctSessionIdsByUser(testUser);
    }

    @Test
    void getUserMessages_DeveRetornarMensagensComPaginacao() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<ChatMessage> messages = Arrays.asList(testMessage);
        Page<ChatMessage> messagePage = new PageImpl<>(messages, pageable, 1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findByUserOrderByCreatedAtDesc(testUser, pageable))
                .thenReturn(messagePage);

        // Act
        Page<ChatResponseDTO> result = chatService.getUserMessages(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        ChatResponseDTO responseDto = result.getContent().get(0);
        assertEquals(testMessage.getId(), responseDto.getMessageId());

        verify(userRepository).findByEmail("test@example.com");
        verify(chatMessageRepository).findByUserOrderByCreatedAtDesc(testUser, pageable);
    }

    @Test
    void getChatStatistics_DeveRetornarEstatisticas() {
        // Arrange
        when(chatMessageRepository.count()).thenReturn(100L);
        when(chatMessageRepository.countDistinctSessions()).thenReturn(25L);
        when(chatMessageRepository.countDistinctUsers()).thenReturn(15L);
        when(chatMessageRepository.findActiveSessionsSince(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        when(chatMessageRepository.countMessagesByStatus())
                .thenReturn(Arrays.asList(
                        new Object[] { ChatMessage.MessageStatus.RESPONDED, 80L },
                        new Object[] { ChatMessage.MessageStatus.PENDING, 20L }));
        when(chatMessageRepository.countMessagesByType())
                .thenReturn(Arrays.asList(
                        new Object[] { ChatMessage.MessageType.NUTRITION_QUESTION, 40L },
                        new Object[] { ChatMessage.MessageType.GENERAL_QUESTION, 60L }));

        // Act
        ChatService.ChatStatsDTO stats = chatService.getChatStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalMessages());
        assertEquals(25L, stats.getTotalSessions());
        assertEquals(15L, stats.getTotalUsers());
        assertEquals(2, stats.getActiveSessionsLast24h());
        assertNotNull(stats.getMessagesByStatus());
        assertNotNull(stats.getMessagesByType());

        verify(chatMessageRepository).count();
        verify(chatMessageRepository).countDistinctSessions();
        verify(chatMessageRepository).countDistinctUsers();
    }

    @Test
    void generateAutomaticResponse_DeveGerarRespostaParaNutricao() {
        // Arrange
        testRequest.setMessageType(ChatMessage.MessageType.NUTRITION_QUESTION);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        // Act
        ChatResponseDTO response = chatService.sendMessage(testRequest);

        // Assert
        assertNotNull(response.getResponseContent());
        assertTrue(response.getResponseContent().contains("nutrição"));
        assertTrue(response.getResponseContent().contains("planos nutricionais"));
        assertEquals(ChatMessage.MessageStatus.RESPONDED, response.getStatus());
    }

    @Test
    void generateAutomaticResponse_DeveGerarRespostaParaAssinatura() {
        // Arrange
        testRequest.setMessageType(ChatMessage.MessageType.SUBSCRIPTION_QUESTION);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        // Act
        ChatResponseDTO response = chatService.sendMessage(testRequest);

        // Assert
        assertNotNull(response.getResponseContent());
        assertTrue(response.getResponseContent().contains("assinatura"));
        assertTrue(response.getResponseContent().contains("planos"));
        assertEquals(ChatMessage.MessageStatus.RESPONDED, response.getStatus());
    }

    @Test
    void generateAutomaticResponse_DeveGerarRespostaParaSuporteTecnico() {
        // Arrange
        testRequest.setMessageType(ChatMessage.MessageType.TECHNICAL_SUPPORT);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        // Act
        ChatResponseDTO response = chatService.sendMessage(testRequest);

        // Assert
        assertNotNull(response.getResponseContent());
        assertTrue(response.getResponseContent().contains("técnicas"));
        assertTrue(response.getResponseContent().contains("suporte técnico"));
        assertEquals(ChatMessage.MessageStatus.RESPONDED, response.getStatus());
    }
}
