package fiap.backend.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.backend.domain.Subscription;
import fiap.backend.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class SubscriptionIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void testCompleteSubscriptionFlow() throws Exception {
                // 1. Create subscription plan as admin
                SubscriptionPlanRequest planRequest = new SubscriptionPlanRequest("Integration Test Plan",
                                BigDecimal.valueOf(29.99));

                String planResponse = mockMvc.perform(post("/api/v1/subscription-plans")
                                .with(csrf())
                                .with(mockUser("admin@nutrixpert.local", "ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(planRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Integration Test Plan"))
                                .andExpect(jsonPath("$.price").value(29.99))
                                .andReturn().getResponse().getContentAsString();

                SubscriptionPlanResponse plan = objectMapper.readValue(planResponse, SubscriptionPlanResponse.class);

                // 2. User creates subscription
                SubscriptionRequest subscriptionRequest = new SubscriptionRequest(plan.getId());

                String subscriptionResponse = mockMvc.perform(post("/api/v1/subscriptions")
                                .with(csrf())
                                .with(mockUser("user@nutrixpert.local", "USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(subscriptionRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.planId").value(plan.getId().toString()))
                                .andReturn().getResponse().getContentAsString();

                SubscriptionResponse subscription = objectMapper.readValue(subscriptionResponse,
                                SubscriptionResponse.class);

                // 3. Admin views pending subscriptions
                mockMvc.perform(get("/api/v1/subscriptions/pending")
                                .with(mockUser("admin@nutrixpert.local", "ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(subscription.getId().toString()))
                                .andExpect(jsonPath("$[0].status").value("PENDING"));

                // 4. Admin approves subscription
                mockMvc.perform(patch("/api/v1/subscriptions/{id}/approve", subscription.getId())
                                .with(csrf())
                                .with(mockUser("admin@nutrixpert.local", "ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                // 5. User views their subscriptions
                mockMvc.perform(get("/api/v1/subscriptions")
                                .with(mockUser("user@nutrixpert.local", "USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].status").value("APPROVED"));

                // 6. User cancels subscription
                mockMvc.perform(patch("/api/v1/subscriptions/{id}/cancel", subscription.getId())
                                .with(csrf())
                                .with(mockUser("user@nutrixpert.local", "USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @WithMockUser(username = "user@nutrixpert.local", roles = "USER")
        void testUserAccessRestrictions() throws Exception {
                // User cannot access admin endpoints
                mockMvc.perform(get("/api/v1/subscription-plans/all"))
                                .andExpect(status().isForbidden());

                mockMvc.perform(get("/api/v1/subscriptions/all"))
                                .andExpect(status().isForbidden());

                mockMvc.perform(get("/api/v1/subscriptions/pending"))
                                .andExpect(status().isForbidden());

                mockMvc.perform(post("/api/v1/subscription-plans")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Test\",\"price\":10.0}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testPublicEndpoints() throws Exception {
                // Public endpoints should work without authentication
                mockMvc.perform(get("/api/v1/subscription-plans"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/auth/health"))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "admin@nutrixpert.local", roles = "ADMIN")
        void testSubscriptionPlanCrud() throws Exception {
                // Create
                SubscriptionPlanRequest createRequest = new SubscriptionPlanRequest("CRUD Test Plan",
                                BigDecimal.valueOf(15.99));
                String createResponse = mockMvc.perform(post("/api/v1/subscription-plans")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                SubscriptionPlanResponse createdPlan = objectMapper.readValue(createResponse,
                                SubscriptionPlanResponse.class);

                // Read
                mockMvc.perform(get("/api/v1/subscription-plans/{id}", createdPlan.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("CRUD Test Plan"));

                // Update
                SubscriptionPlanRequest updateRequest = new SubscriptionPlanRequest("Updated CRUD Plan",
                                BigDecimal.valueOf(25.99));
                mockMvc.perform(put("/api/v1/subscription-plans/{id}", createdPlan.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated CRUD Plan"))
                                .andExpect(jsonPath("$.price").value(25.99));

                // Deactivate
                mockMvc.perform(patch("/api/v1/subscription-plans/{id}/deactivate", createdPlan.getId())
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                // Activate
                mockMvc.perform(patch("/api/v1/subscription-plans/{id}/activate", createdPlan.getId())
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                // Delete
                mockMvc.perform(delete("/api/v1/subscription-plans/{id}", createdPlan.getId())
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "admin@nutrixpert.local", roles = "ADMIN")
        void testSubscriptionStatusUpdates() throws Exception {
                // Create plan first
                SubscriptionPlanRequest planRequest = new SubscriptionPlanRequest("Status Test Plan",
                                BigDecimal.valueOf(19.99));
                String planResponse = mockMvc.perform(post("/api/v1/subscription-plans")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(planRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                SubscriptionPlanResponse plan = objectMapper.readValue(planResponse, SubscriptionPlanResponse.class);

                // Create subscription
                SubscriptionRequest subscriptionRequest = new SubscriptionRequest(plan.getId());
                String subscriptionResponse = mockMvc.perform(post("/api/v1/subscriptions")
                                .with(csrf())
                                .with(mockUser("user@nutrixpert.local", "USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(subscriptionRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                SubscriptionResponse subscription = objectMapper.readValue(subscriptionResponse,
                                SubscriptionResponse.class);

                // Test reject
                mockMvc.perform(patch("/api/v1/subscriptions/{id}/reject", subscription.getId())
                                .with(csrf())
                                .with(mockUser("admin@nutrixpert.local", "ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("REJECTED"));

                // Test status update with generic endpoint
                SubscriptionStatusUpdateRequest statusRequest = new SubscriptionStatusUpdateRequest(
                                Subscription.SubscriptionStatus.CANCELLED);
                mockMvc.perform(patch("/api/v1/subscriptions/{id}/status", subscription.getId())
                                .with(csrf())
                                .with(mockUser("admin@nutrixpert.local", "ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void testUserCanHaveOnlyOneActivePlan() throws Exception {
                // 1. Criar primeiro plano básico
                SubscriptionPlanRequest basicPlanRequest = new SubscriptionPlanRequest("Basic Plan",
                                BigDecimal.valueOf(29.99));

                String basicPlanResponse = mockMvc.perform(post("/api/v1/subscription-plans")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(basicPlanRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                SubscriptionPlanResponse basicPlan = objectMapper.readValue(basicPlanResponse,
                                SubscriptionPlanResponse.class);

                // 2. Criar segundo plano premium
                SubscriptionPlanRequest premiumPlanRequest = new SubscriptionPlanRequest("Premium Plan",
                                BigDecimal.valueOf(59.99));

                String premiumPlanResponse = mockMvc.perform(post("/api/v1/subscription-plans")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(premiumPlanRequest)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                SubscriptionPlanResponse premiumPlan = objectMapper.readValue(premiumPlanResponse,
                                SubscriptionPlanResponse.class);

                // 3. Usuário solicita primeira assinatura (basic)
                SubscriptionRequest basicSubscriptionRequest = new SubscriptionRequest();
                basicSubscriptionRequest.setPlanId(basicPlan.getId());

                String basicSubscriptionResponse = mockMvc.perform(post("/api/v1/subscriptions")
                                .with(csrf())
                                .with(mockUser("user@test.com", "USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(basicSubscriptionRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn().getResponse().getContentAsString();

                SubscriptionResponse basicSubscription = objectMapper.readValue(basicSubscriptionResponse,
                                SubscriptionResponse.class);

                // 4. Admin aprova primeira assinatura
                SubscriptionStatusUpdateRequest approvalRequest = new SubscriptionStatusUpdateRequest();
                approvalRequest.setStatus(Subscription.SubscriptionStatus.APPROVED);

                mockMvc.perform(patch("/api/v1/subscriptions/{id}/status", basicSubscription.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(approvalRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                // 5. Usuário solicita upgrade para premium
                SubscriptionRequest premiumSubscriptionRequest = new SubscriptionRequest();
                premiumSubscriptionRequest.setPlanId(premiumPlan.getId());

                String premiumSubscriptionResponse = mockMvc.perform(post("/api/v1/subscriptions")
                                .with(csrf())
                                .with(mockUser("user@test.com", "USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(premiumSubscriptionRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn().getResponse().getContentAsString();

                SubscriptionResponse premiumSubscription = objectMapper.readValue(premiumSubscriptionResponse,
                                SubscriptionResponse.class);

                // 6. Admin aprova upgrade para premium
                SubscriptionStatusUpdateRequest premiumApprovalRequest = new SubscriptionStatusUpdateRequest();
                premiumApprovalRequest.setStatus(Subscription.SubscriptionStatus.APPROVED);

                mockMvc.perform(patch("/api/v1/subscriptions/{id}/status", premiumSubscription.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(premiumApprovalRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                // 7. Verificar que apenas a assinatura premium está ativa
                // A assinatura básica deve ter sido automaticamente cancelada
                mockMvc.perform(get("/api/v1/subscriptions/{id}", basicSubscription.getId())
                                .with(mockUser("user@test.com", "USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELLED"));

                mockMvc.perform(get("/api/v1/subscriptions/{id}", premiumSubscription.getId())
                                .with(mockUser("user@test.com", "USER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        // Helper method to create mock user with email and roles
        private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor mockUser(
                        String email, String... roles) {
                return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(email)
                                .roles(roles);
        }
}
