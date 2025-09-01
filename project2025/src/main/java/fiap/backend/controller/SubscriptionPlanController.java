package fiap.backend.controller;

import fiap.backend.dto.SubscriptionPlanRequest;
import fiap.backend.dto.SubscriptionPlanResponse;
import fiap.backend.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@Tag(name = "Planos de Subscrição", description = "APIs para gerenciar planos de subscrição")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @GetMapping
    @Operation(summary = "Listar planos ativos", description = "Retorna todos os planos de subscrição ativos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de planos retornada com sucesso",
                     content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class)))
    })
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllActivePlans() {
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllActivePlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os planos", description = "Retorna todos os planos (incluindo inativos) - Apenas administradores")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista completa de planos retornada",
                     content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado - Apenas administradores")
    })
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter plano por ID", description = "Retorna um plano específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Plano encontrado",
                     content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))),
        @ApiResponse(responseCode = "404", description = "Plano não encontrado")
    })
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(@PathVariable UUID id) {
        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(@Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlanResponse plan = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(@PathVariable UUID id,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlanResponse plan = subscriptionPlanService.updatePlan(id, request);
        return ResponseEntity.ok(plan);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable UUID id) {
        subscriptionPlanService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activatePlan(@PathVariable UUID id) {
        subscriptionPlanService.activatePlan(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
