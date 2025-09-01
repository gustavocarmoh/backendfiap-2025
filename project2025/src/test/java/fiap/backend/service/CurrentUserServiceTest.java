package fiap.backend.service;

import fiap.backend.domain.User;
import fiap.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CurrentUserService currentUserService;

    private User testUser;
    private String testEmail;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testEmail = "user@test.com";
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUserId_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UUID result = currentUserService.getCurrentUserId();

        // Then
        assertEquals(testUserId, result);
        verify(authentication).getName();
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void getCurrentUserId_UserNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> currentUserService.getCurrentUserId());
        assertEquals("Current user not found", exception.getMessage());
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void getCurrentUser_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        User result = currentUserService.getCurrentUser();

        // Then
        assertEquals(testUser, result);
        assertEquals(testUserId, result.getId());
        assertEquals(testEmail, result.getEmail());
        verify(authentication).getName();
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void getCurrentUser_UserNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> currentUserService.getCurrentUser());
        assertEquals("Current user not found", exception.getMessage());
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void getCurrentUserEmail_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testEmail);

        // When
        String result = currentUserService.getCurrentUserEmail();

        // Then
        assertEquals(testEmail, result);
        verify(authentication).isAuthenticated();
        verify(authentication).getName();
    }

    @Test
    void getCurrentUserEmail_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> currentUserService.getCurrentUserEmail());
        assertEquals("User not authenticated", exception.getMessage());
        verify(authentication).isAuthenticated();
        verify(authentication, never()).getName();
    }

    @Test
    void getCurrentUserEmail_NullAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> currentUserService.getCurrentUserEmail());
        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void getCurrentUserId_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> currentUserService.getCurrentUserId());
        assertEquals("User not authenticated", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getCurrentUser_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> currentUserService.getCurrentUser());
        assertEquals("User not authenticated", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }
}
