package fiap.backend.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.backend.domain.Subscription;
import fiap.backend.dto.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class JwtTokenPlanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mesma chave usada na aplicação
    private final String jwtSecret = "minha-super-chave-secreta-jwt-de-32-caracteres-segura-para-desenvolvimento-local";

    @Test
    void testLoginJwtContainsPlanInfo() throws Exception {
        // 1. Registrar usuário
        UserRegisterRequest registerRequest = new UserRegisterRequest("Test User", "jwt.test@example.com",
                "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Criar plano de assinatura
        SubscriptionPlanRequest planRequest = new SubscriptionPlanRequest("JWT Test Plan",
                BigDecimal.valueOf(19.99));

        String planResponse = mockMvc.perform(post("/api/v1/subscription-plans")
                .with(csrf())
                .with(mockUser("admin@test.com", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        SubscriptionPlanResponse plan = objectMapper.readValue(planResponse, SubscriptionPlanResponse.class);

        // 3. Usuário solicita assinatura
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setPlanId(plan.getId());

        String subscriptionResponse = mockMvc.perform(post("/api/v1/subscriptions")
                .with(csrf())
                .with(mockUser("jwt.test@example.com", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        SubscriptionResponse subscription = objectMapper.readValue(subscriptionResponse, SubscriptionResponse.class);

        // 4. Admin aprova assinatura
        SubscriptionStatusUpdateRequest approvalRequest = new SubscriptionStatusUpdateRequest();
        approvalRequest.setStatus(Subscription.SubscriptionStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/subscriptions/{id}/status", subscription.getId())
                .with(csrf())
                .with(mockUser("admin@test.com", "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        // 5. Fazer login e verificar se o token contém informações do plano
        UserLoginRequest loginRequest = new UserLoginRequest("jwt.test@example.com", "password123");

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // 6. Decodificar o token e verificar as claims do plano
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Verificar se as informações do plano estão no token
        assertEquals("jwt.test@example.com", claims.getSubject());
        assertEquals("JWT Test Plan", claims.get("activePlanName"));
        assertEquals(plan.getId().toString(), claims.get("activePlanId"));

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("USER"));
    }

    @Test
    void testLoginWithoutActivePlan() throws Exception {
        // 1. Registrar usuário
        UserRegisterRequest registerRequest = new UserRegisterRequest("No Plan User", "noplan@example.com",
                "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Fazer login (usuário sem plano ativo)
        UserLoginRequest loginRequest = new UserLoginRequest("noplan@example.com", "password123");

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // 3. Decodificar o token e verificar que não há informações de plano
        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Verificar que não há informações de plano no token
        assertEquals("noplan@example.com", claims.getSubject());
        assertNull(claims.get("activePlanName"));
        assertNull(claims.get("activePlanId"));
    }

    @Test
    @WithMockUser(username = "jwt.test@example.com", roles = "USER")
    void testUserInfoEndpointReturnsActivePlan() throws Exception {
        // Este teste simularia uma requisição autenticada com token contendo plano
        // ativo
        // Como é difícil simular o token completo em testes unitários,
        // este endpoint seria testado em testes de sistema reais

        mockMvc.perform(get("/api/v1/user/me")
                .with(request -> {
                    request.setAttribute("activePlanName", "Test Plan");
                    request.setAttribute("activePlanId", "12345");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasActivePlan").value(true))
                .andExpect(jsonPath("$.activePlanName").value("Test Plan"))
                .andExpect(jsonPath("$.activePlanId").value("12345"));
    }

    // Helper method
    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor mockUser(
            String email, String... roles) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(email)
                .roles(roles);
    }
}
