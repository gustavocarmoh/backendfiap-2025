package fiap.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;

import fiap.backend.domain.Role;
import fiap.backend.domain.User;
import fiap.backend.dto.AuthResponse;
import fiap.backend.dto.UserLoginRequest;
import fiap.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DelegatingPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Create mock user
        mockUser = new User();
        mockUser.setEmail("admin@nutrixpert.local");
        mockUser.setPasswordHash("{noop}admin123");
        mockUser.setFirstName("System");
        mockUser.setLastName("Admin");
        mockUser.setIsActive(true);

        // Create mock role
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        mockUser.setRoles(Set.of(adminRole));

        // Create login request
        loginRequest = new UserLoginRequest("admin@nutrixpert.local", "admin123");
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByEmail("admin@nutrixpert.local"))
                .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("admin123", "{noop}admin123"))
                .thenReturn(true);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
        System.out.println("Login successful! JWT Token: " + response.getToken());
    }

    @Test
    void testLoginWithWrongPassword() {
        // Arrange
        when(userRepository.findByEmail("admin@nutrixpert.local"))
                .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "{noop}admin123"))
                .thenReturn(false);

        loginRequest = new UserLoginRequest("admin@nutrixpert.local", "wrongpassword");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Usuário ou senha inválidos", exception.getMessage());
    }

    @Test
    void testLoginWithNonExistentUser() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        loginRequest = new UserLoginRequest("nonexistent@example.com", "admin123");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Usuário ou senha inválidos", exception.getMessage());
    }
}
