package fiap.backend.service;

import fiap.backend.domain.User;
import fiap.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServicePlanTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    private User testUser;
    private UUID userId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test@example.com";
        
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(userEmail);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
    }

    @Test
    void getCurrentUserId_Success() {
        // Given
        setupAuthentication();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // When
        UUID result = currentUserService.getCurrentUserId();

        // Then
        assertEquals(userId, result);
        verify(userRepository).findByEmail(userEmail);
    }

    @Test
    void getCurrentUserEmail_Success() {
        // Given
        setupAuthentication();

        // When
        String result = currentUserService.getCurrentUserEmail();

        // Then
        assertEquals(userEmail, result);
    }

    @Test
    void getCurrentUserActivePlanName_WithActivePlan() {
        // Given
        setupAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("activePlanName", "Premium Plan");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        String result = currentUserService.getCurrentUserActivePlanName();

        // Then
        assertEquals("Premium Plan", result);
    }

    @Test
    void getCurrentUserActivePlanName_WithoutActivePlan() {
        // Given
        setupAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        String result = currentUserService.getCurrentUserActivePlanName();

        // Then
        assertNull(result);
    }

    @Test
    void getCurrentUserActivePlanId_WithActivePlan() {
        // Given
        setupAuthentication();
        String planId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("activePlanId", planId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        String result = currentUserService.getCurrentUserActivePlanId();

        // Then
        assertEquals(planId, result);
    }

    @Test
    void hasActivePlan_True() {
        // Given
        setupAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("activePlanName", "Basic Plan");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        boolean result = currentUserService.hasActivePlan();

        // Then
        assertTrue(result);
    }

    @Test
    void hasActivePlan_False() {
        // Given
        setupAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        boolean result = currentUserService.hasActivePlan();

        // Then
        assertFalse(result);
    }

    @Test
    void getCurrentUserPlanInfo_WithActivePlan() {
        // Given
        setupAuthentication();
        String planId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("activePlanName", "Premium Plan");
        request.setAttribute("activePlanId", planId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // When
        CurrentUserService.UserPlanInfo result = currentUserService.getCurrentUserPlanInfo();

        // Then
        assertNotNull(result);
        assertEquals(userEmail, result.getEmail());
        assertEquals(userId, result.getUserId());
        assertEquals("Premium Plan", result.getActivePlanName());
        assertEquals(planId, result.getActivePlanId());
        assertTrue(result.hasActivePlan());
    }

    @Test
    void getCurrentUserPlanInfo_WithoutActivePlan() {
        // Given
        setupAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // When
        CurrentUserService.UserPlanInfo result = currentUserService.getCurrentUserPlanInfo();

        // Then
        assertNotNull(result);
        assertEquals(userEmail, result.getEmail());
        assertEquals(userId, result.getUserId());
        assertNull(result.getActivePlanName());
        assertNull(result.getActivePlanId());
        assertFalse(result.hasActivePlan());
    }

    private void setupAuthentication() {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userEmail, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
