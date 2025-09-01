package fiap.backend.controller;

import fiap.backend.dto.AdminDashboardDTO;
import fiap.backend.dto.GrowthMetricsDTO;
import fiap.backend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller para dashboard administrativo com métricas de negócio
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Dashboard administrativo com métricas e analytics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping
    @Operation(summary = "Dashboard completo", description = "Retorna todas as métricas principais do dashboard administrativo")
    @ApiResponse(responseCode = "200", description = "Dashboard gerado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/growth")
    @Operation(summary = "Métricas de crescimento", description = "Retorna métricas de crescimento por período (daily, weekly, monthly)")
    @ApiResponse(responseCode = "200", description = "Métricas de crescimento geradas")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    public ResponseEntity<GrowthMetricsDTO> getGrowthMetrics(
            @Parameter(description = "Data de início (formato: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Data de fim (formato: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Período de agrupamento", example = "daily") @RequestParam(defaultValue = "daily") String period) {

        // Validação básica de datas
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }

        // Validação do período
        if (!period.matches("(?i)(daily|weekly|monthly)")) {
            throw new IllegalArgumentException("Período deve ser: daily, weekly ou monthly");
        }

        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, period);
        return ResponseEntity.ok(growthMetrics);
    }

    @GetMapping("/growth/last-30-days")
    @Operation(summary = "Crescimento dos últimos 30 dias", description = "Métricas de crescimento dos últimos 30 dias (agrupamento diário)")
    @ApiResponse(responseCode = "200", description = "Métricas dos últimos 30 dias")
    public ResponseEntity<GrowthMetricsDTO> getLast30DaysGrowth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, "daily");
        return ResponseEntity.ok(growthMetrics);
    }

    @GetMapping("/growth/last-12-months")
    @Operation(summary = "Crescimento dos últimos 12 meses", description = "Métricas de crescimento dos últimos 12 meses (agrupamento mensal)")
    @ApiResponse(responseCode = "200", description = "Métricas dos últimos 12 meses")
    public ResponseEntity<GrowthMetricsDTO> getLast12MonthsGrowth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);

        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, "monthly");
        return ResponseEntity.ok(growthMetrics);
    }

    @GetMapping("/growth/this-year")
    @Operation(summary = "Crescimento do ano atual", description = "Métricas de crescimento desde o início do ano (agrupamento mensal)")
    @ApiResponse(responseCode = "200", description = "Métricas do ano atual")
    public ResponseEntity<GrowthMetricsDTO> getThisYearGrowth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = LocalDate.of(endDate.getYear(), 1, 1);

        GrowthMetricsDTO growthMetrics = adminDashboardService.generateGrowthMetrics(startDate, endDate, "monthly");
        return ResponseEntity.ok(growthMetrics);
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumo executivo", description = "Versão simplificada do dashboard com métricas principais")
    @ApiResponse(responseCode = "200", description = "Resumo executivo gerado")
    public ResponseEntity<AdminDashboardDTO> getExecutiveSummary() {
        // Por enquanto retorna o dashboard completo, mas pode ser otimizado
        // para retornar apenas métricas essenciais
        AdminDashboardDTO dashboard = adminDashboardService.generateDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
