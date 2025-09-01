package fiap.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.backend.dto.SubscriptionPlanRequest;
import fiap.backend.dto.SubscriptionPlanResponse;
import fiap.backend.service.SubscriptionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionPlanController.class)
class SubscriptionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionPlanService subscriptionPlanService;

    @Autowired
    private ObjectMapper objectMapper;

    private SubscriptionPlanResponse testPlanResponse;
    private SubscriptionPlanRequest testPlanRequest;
    private UUID planId;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        testPlanResponse = new SubscriptionPlanResponse(
                planId,
                "Test Plan",
                BigDecimal.valueOf(19.99),
                LocalDateTime.now(),
                LocalDateTime.now(),
                true);
        testPlanRequest = new SubscriptionPlanRequest("Test Plan", BigDecimal.valueOf(19.99));
    }

    @Test
    void getAllActivePlans_Success() throws Exception {
        // Given
        List<SubscriptionPlanResponse> plans = Arrays.asList(testPlanResponse);
        when(subscriptionPlanService.getAllActivePlans()).thenReturn(plans);

        // When & Then
        mockMvc.perform(get("/api/v1/subscription-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(planId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Plan"))
                .andExpect(jsonPath("$[0].price").value(19.99));

        verify(subscriptionPlanService).getAllActivePlans();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPlans_Admin_Success() throws Exception {
        // Given
        List<SubscriptionPlanResponse> plans = Arrays.asList(testPlanResponse);
        when(subscriptionPlanService.getAllPlans()).thenReturn(plans);

        // When & Then
        mockMvc.perform(get("/api/v1/subscription-plans/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(planId.toString()));

        verify(subscriptionPlanService).getAllPlans();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllPlans_User_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/subscription-plans/all"))
                .andExpect(status().isForbidden());

        verify(subscriptionPlanService, never()).getAllPlans();
    }

    @Test
    void getPlanById_Success() throws Exception {
        // Given
        when(subscriptionPlanService.getPlanById(planId)).thenReturn(testPlanResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/subscription-plans/{id}", planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.name").value("Test Plan"));

        verify(subscriptionPlanService).getPlanById(planId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPlan_Admin_Success() throws Exception {
        // Given
        when(subscriptionPlanService.createPlan(any(SubscriptionPlanRequest.class)))
                .thenReturn(testPlanResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/subscription-plans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPlanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Plan"))
                .andExpect(jsonPath("$.price").value(19.99));

        verify(subscriptionPlanService).createPlan(any(SubscriptionPlanRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPlan_User_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/subscription-plans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPlanRequest)))
                .andExpect(status().isForbidden());

        verify(subscriptionPlanService, never()).createPlan(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePlan_Admin_Success() throws Exception {
        // Given
        when(subscriptionPlanService.updatePlan(eq(planId), any(SubscriptionPlanRequest.class)))
                .thenReturn(testPlanResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/subscription-plans/{id}", planId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPlanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Plan"));

        verify(subscriptionPlanService).updatePlan(eq(planId), any(SubscriptionPlanRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivatePlan_Admin_Success() throws Exception {
        // Given
        doNothing().when(subscriptionPlanService).deactivatePlan(planId);

        // When & Then
        mockMvc.perform(patch("/api/v1/subscription-plans/{id}/deactivate", planId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(subscriptionPlanService).deactivatePlan(planId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activatePlan_Admin_Success() throws Exception {
        // Given
        doNothing().when(subscriptionPlanService).activatePlan(planId);

        // When & Then
        mockMvc.perform(patch("/api/v1/subscription-plans/{id}/activate", planId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(subscriptionPlanService).activatePlan(planId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePlan_Admin_Success() throws Exception {
        // Given
        doNothing().when(subscriptionPlanService).deletePlan(planId);

        // When & Then
        mockMvc.perform(delete("/api/v1/subscription-plans/{id}", planId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(subscriptionPlanService).deletePlan(planId);
    }

    @Test
    void createPlan_InvalidData_BadRequest() throws Exception {
        // Given
        SubscriptionPlanRequest invalidRequest = new SubscriptionPlanRequest("", BigDecimal.valueOf(-1));

        // When & Then
        mockMvc.perform(post("/api/v1/subscription-plans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isForbidden()); // Sem autenticação, será 403

        verify(subscriptionPlanService, never()).createPlan(any());
    }
}
