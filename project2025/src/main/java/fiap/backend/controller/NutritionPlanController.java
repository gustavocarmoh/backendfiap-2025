package fiap.backend.controller;

import fiap.backend.dto.*;
import fiap.backend.service.NutritionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller para gerenciamento de planos nutricionais
 */
@RestController
@RequestMapping("/api/nutrition-plans")
@Tag(name = "Nutrition Plans", description = "Gerenciamento de planos nutricionais diários")
@SecurityRequirement(name = "bearerAuth")
public class NutritionPlanController {

    @Autowired
    private NutritionPlanService nutritionPlanService;

    @PostMapping
    @Operation(summary = "Criar novo plano nutricional", description = "Cria um novo plano nutricional para o usuário autenticado. "
            +
            "Verifica os limites do plano de assinatura do usuário.")
    @ApiResponse(responseCode = "201", description = "Plano nutricional criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou limite de planos atingido")
    @ApiResponse(responseCode = "409", description = "Já existe um plano para esta data")
    public ResponseEntity<NutritionPlanResponseDTO> createNutritionPlan(
            Authentication authentication,
            @Valid @RequestBody CreateNutritionPlanDTO createDTO) {

        String userEmail = authentication.getName();
        NutritionPlanResponseDTO response = nutritionPlanService.createNutritionPlan(userEmail, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Buscar plano nutricional por ID", description = "Busca um plano nutricional específico pelo ID")
    @ApiResponse(responseCode = "200", description = "Plano nutricional encontrado")
    @ApiResponse(responseCode = "404", description = "Plano nutricional não encontrado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<NutritionPlanResponseDTO> getNutritionPlan(
            Authentication authentication,
            @Parameter(description = "ID do plano nutricional") @PathVariable UUID planId) {

        String userEmail = authentication.getName();
        NutritionPlanResponseDTO response = nutritionPlanService.getNutritionPlanById(userEmail, planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @Operation(summary = "Buscar plano nutricional de hoje", description = "Retorna o plano nutricional do dia atual do usuário")
    @ApiResponse(responseCode = "200", description = "Plano de hoje encontrado")
    @ApiResponse(responseCode = "204", description = "Nenhum plano para hoje")
    public ResponseEntity<NutritionPlanResponseDTO> getTodayNutritionPlan(Authentication authentication) {
        String userEmail = authentication.getName();
        Optional<NutritionPlanResponseDTO> todayPlan = nutritionPlanService.getTodayNutritionPlan(userEmail);

        return todayPlan
                .map(plan -> ResponseEntity.ok(plan))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/week")
    @Operation(summary = "Buscar planos nutricionais da semana", description = "Retorna os planos nutricionais da semana atual com estatísticas")
    @ApiResponse(responseCode = "200", description = "Planos da semana retornados")
    public ResponseEntity<WeeklyNutritionPlanResponseDTO> getWeeklyNutritionPlans(Authentication authentication) {
        String userEmail = authentication.getName();
        WeeklyNutritionPlanResponseDTO response = nutritionPlanService.getWeeklyNutritionPlans(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/week/{date}")
    @Operation(summary = "Buscar planos nutricionais de uma semana específica", description = "Retorna os planos nutricionais da semana que contém a data especificada")
    @ApiResponse(responseCode = "200", description = "Planos da semana retornados")
    public ResponseEntity<WeeklyNutritionPlanResponseDTO> getWeeklyNutritionPlans(
            Authentication authentication,
            @Parameter(description = "Data de referência para a semana (formato: yyyy-MM-dd)") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String userEmail = authentication.getName();
        WeeklyNutritionPlanResponseDTO response = nutritionPlanService.getWeeklyNutritionPlans(userEmail, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar planos nutricionais", description = "Lista os planos nutricionais do usuário com paginação")
    @ApiResponse(responseCode = "200", description = "Lista de planos retornada")
    public ResponseEntity<Page<NutritionPlanResponseDTO>> getUserNutritionPlans(
            Authentication authentication,
            @Parameter(description = "Número da página (inicia em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size) {

        String userEmail = authentication.getName();
        Page<NutritionPlanResponseDTO> response = nutritionPlanService.getUserNutritionPlans(userEmail, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Buscar planos pendentes", description = "Retorna os planos nutricionais que ainda não foram completados")
    @ApiResponse(responseCode = "200", description = "Planos pendentes retornados")
    public ResponseEntity<List<NutritionPlanResponseDTO>> getPendingPlans(Authentication authentication) {
        String userEmail = authentication.getName();
        List<NutritionPlanResponseDTO> response = nutritionPlanService.getPendingPlans(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/future")
    @Operation(summary = "Buscar planos futuros", description = "Retorna os planos nutricionais programados para o futuro")
    @ApiResponse(responseCode = "200", description = "Planos futuros retornados")
    public ResponseEntity<List<NutritionPlanResponseDTO>> getFuturePlans(Authentication authentication) {
        String userEmail = authentication.getName();
        List<NutritionPlanResponseDTO> response = nutritionPlanService.getFuturePlans(userEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Atualizar plano nutricional", description = "Atualiza um plano nutricional existente")
    @ApiResponse(responseCode = "200", description = "Plano nutricional atualizado")
    @ApiResponse(responseCode = "404", description = "Plano nutricional não encontrado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<NutritionPlanResponseDTO> updateNutritionPlan(
            Authentication authentication,
            @Parameter(description = "ID do plano nutricional") @PathVariable UUID planId,
            @Valid @RequestBody UpdateNutritionPlanDTO updateDTO) {

        String userEmail = authentication.getName();
        NutritionPlanResponseDTO response = nutritionPlanService.updateNutritionPlan(userEmail, planId, updateDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{planId}/completion")
    @Operation(summary = "Alternar conclusão do plano", description = "Marca um plano como completo ou incompleto")
    @ApiResponse(responseCode = "200", description = "Status de conclusão alterado")
    @ApiResponse(responseCode = "404", description = "Plano nutricional não encontrado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<NutritionPlanResponseDTO> togglePlanCompletion(
            Authentication authentication,
            @Parameter(description = "ID do plano nutricional") @PathVariable UUID planId) {

        String userEmail = authentication.getName();
        NutritionPlanResponseDTO response = nutritionPlanService.togglePlanCompletion(userEmail, planId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Excluir plano nutricional", description = "Remove um plano nutricional")
    @ApiResponse(responseCode = "204", description = "Plano nutricional excluído")
    @ApiResponse(responseCode = "404", description = "Plano nutricional não encontrado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<Void> deleteNutritionPlan(
            Authentication authentication,
            @Parameter(description = "ID do plano nutricional") @PathVariable UUID planId) {

        String userEmail = authentication.getName();
        nutritionPlanService.deleteNutritionPlan(userEmail, planId);
        return ResponseEntity.noContent().build();
    }
}
