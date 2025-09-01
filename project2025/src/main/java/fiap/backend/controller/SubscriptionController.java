package fiap.backend.controller;

import fiap.backend.domain.Subscription;
import fiap.backend.dto.SubscriptionRequest;
import fiap.backend.dto.SubscriptionResponse;
import fiap.backend.dto.SubscriptionStatusUpdateRequest;
import fiap.backend.service.CurrentUserService;
import fiap.backend.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscrições", description = "APIs para gerenciar subscrições de usuários")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUserService currentUserService;

    public SubscriptionController(SubscriptionService subscriptionService, CurrentUserService currentUserService) {
        this.subscriptionService = subscriptionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @Operation(summary = "Criar nova subscrição", description = "Cria uma nova subscrição para o usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscrição criada com sucesso", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.createSubscription(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @GetMapping
    @Operation(summary = "Listar subscrições do usuário", description = "Retorna todas as subscrições do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de subscrições retornada com sucesso", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions() {
        UUID userId = currentUserService.getCurrentUserId();
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter subscrição por ID", description = "Retorna uma subscrição específica pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscrição encontrada com sucesso", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Subscrição não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @Parameter(description = "ID da subscrição") @PathVariable UUID id) {
        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas as subscrições (Admin)", description = "Retorna todas as subscrições do sistema - apenas para administradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de todas as subscrições retornada com sucesso", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar subscrições pendentes (Admin)", description = "Retorna todas as subscrições com status pendente - apenas para administradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de subscrições pendentes retornada com sucesso", content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token de acesso inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getPendingSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getPendingSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByStatus(@PathVariable String status) {
        try {
            Subscription.SubscriptionStatus subscriptionStatus = Subscription.SubscriptionStatus
                    .valueOf(status.toUpperCase());
            List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByStatus(subscriptionStatus);
            return ResponseEntity.ok(subscriptions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionStatusUpdateRequest request) {
        UUID adminUserId = currentUserService.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.updateSubscriptionStatus(id, adminUserId, request);
        return ResponseEntity.ok(subscription);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> approveSubscription(@PathVariable UUID id) {
        UUID adminUserId = currentUserService.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.approveSubscription(id, adminUserId);
        return ResponseEntity.ok(subscription);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> rejectSubscription(@PathVariable UUID id) {
        UUID adminUserId = currentUserService.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.rejectSubscription(id, adminUserId);
        return ResponseEntity.ok(subscription);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable UUID id) {
        UUID userId = currentUserService.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.cancelSubscription(id, userId);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveSubscriptionsCount() {
        UUID userId = currentUserService.getCurrentUserId();
        long count = subscriptionService.countActiveSubscriptionsByUserId(userId);
        return ResponseEntity.ok(count);
    }
}
