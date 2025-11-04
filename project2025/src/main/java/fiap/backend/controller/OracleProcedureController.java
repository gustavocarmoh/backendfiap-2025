package fiap.backend.controller;

import fiap.backend.dto.AnaliseNutricionalRequest;
import fiap.backend.dto.AnaliseNutricionalResponse;
import fiap.backend.service.OracleStoredProcedureService;
import fiap.backend.service.llm.LlmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Controller REST para executar Functions e Procedures PL/SQL do Oracle
 * Demonstra integração completa Backend Java → Oracle Database
 */
@RestController
@RequestMapping("/api/v1/oracle")
@Tag(name = "Oracle PL/SQL", description = "Endpoints para execução de Functions e Procedures Oracle PL/SQL")
public class OracleProcedureController {

    private final OracleStoredProcedureService oracleService;
    private final LlmService llmService;
    private static final Logger logger = LoggerFactory.getLogger(OracleProcedureController.class);

    public OracleProcedureController(OracleStoredProcedureService oracleService, LlmService llmService) {
        this.oracleService = oracleService;
        this.llmService = llmService;
    }

    // Function: calcular_indicador_saude_usuario
    @GetMapping("/indicador-saude/{userId}")
    @Operation(summary = "Calcular Indicador de Saúde", description = "Executa a function PL/SQL calcular_indicador_saude_usuario para obter score de saúde nutricional (0-100)")
    public ResponseEntity<Map<String, Object>> calcularIndicadorSaude(
            @Parameter(description = "ID do usuário") @PathVariable String userId,
            @Parameter(description = "Dias de análise (padrão: 30)") @RequestParam(defaultValue = "30") int diasAnalise
    ) {
        logger.info("REST API: Calcular indicador de saúde - userId={}, diasAnalise={}", userId, diasAnalise);

        try {
            Double score = oracleService.calcularIndicadorSaudeUsuario(userId, diasAnalise);

            String classificacao;
            if (score >= 85) {
                classificacao = "EXCELENTE";
            } else if (score >= 70) {
                classificacao = "BOM";
            } else if (score >= 50) {
                classificacao = "REGULAR";
            } else {
                classificacao = "PRECISA MELHORAR";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("userId", userId);
            response.put("diasAnalise", diasAnalise);
            response.put("scoreSaude", score);
            response.put("classificacao", classificacao);
            response.put("mensagem", "Indicador de saúde calculado com sucesso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao calcular indicador de saúde: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("sucesso", false);
            error.put("mensagem", "Erro ao calcular indicador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Function: formatar_relatorio_nutricao
    @GetMapping(value = "/relatorio-nutricao/{userId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Gerar Relatório de Nutrição", description = "Executa a function PL/SQL formatar_relatorio_nutricao para gerar relatório formatado em texto")
    public ResponseEntity<String> gerarRelatorioNutricao(@PathVariable String userId) {
        logger.info("REST API: Gerar relatório de nutrição - userId={}", userId);

        try {
            String relatorio = oracleService.formatarRelatorioNutricao(userId);
            return ResponseEntity.ok(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de nutrição: {}", e.getMessage(), e);
            String errorMsg = "ERRO AO GERAR RELATÓRIO\n========================\nMensagem: " + e.getMessage() + "\n";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    // Procedure: proc_registrar_alerta_nutricional
    @PostMapping("/alertas-nutricionais/{userId}")
    @Operation(summary = "Registrar Alertas Nutricionais", description = "Executa a procedure PL/SQL proc_registrar_alerta_nutricional para analisar e registrar alertas")
    public ResponseEntity<Map<String, Object>> registrarAlertasNutricionais(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") int diasAnalise
    ) {
        logger.info("REST API: Registrar alertas nutricionais - userId={}, diasAnalise={}", userId, diasAnalise);
        try {
            int alertasGerados = oracleService.registrarAlertaNutricional(userId, diasAnalise);

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("userId", userId);
            response.put("diasAnalise", diasAnalise);
            response.put("alertasGerados", alertasGerados);
            response.put("mensagem", alertasGerados + " alerta(s) registrado(s) com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao registrar alertas: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("sucesso", false);
            error.put("mensagem", "Erro ao registrar alertas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Procedure: proc_gerar_relatorio_consumo
    @PostMapping("/relatorio-consumo/{userId}")
    @Operation(summary = "Gerar Relatório de Consumo", description = "Executa a procedure PL/SQL proc_gerar_relatorio_consumo para gerar relatório detalhado")
    public ResponseEntity<Map<String, Object>> relatorioConsumo(@PathVariable String userId) {
        logger.info("REST API: Gerar relatório de consumo - userId={}", userId);

        try {
            Map<String, Object> result = oracleService.gerarRelatorioConsumo(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de consumo: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("sucesso", false);
            error.put("mensagem", "Erro ao gerar relatório: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Procedure: proc_consulta_dinamica_planos (Dynamic SQL)
    @GetMapping("/planos-dinamicos")
    @Operation(summary = "Consulta Dinâmica de Planos", description = "Executa procedure PL/SQL com Dynamic SQL para consultas flexíveis")
    public ResponseEntity<List<Map<String, Object>>> consultaDinamicaPlanos(@RequestParam String where) {
        logger.info("REST API: Consulta dinâmica de planos - where={}", where);

        try {
            List<Map<String, Object>> planos = oracleService.consultaDinamicaPlanos(where);
            return ResponseEntity.ok(planos);
        } catch (Exception e) {
            logger.error("Erro na consulta dinâmica de planos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    // Auditoria: consultar registros da trigger
    @GetMapping("/auditoria/planos")
    @Operation(summary = "Consultar Auditoria de Planos", description = "Consulta registros de auditoria de alterações em planos nutricionais")
    public ResponseEntity<List<Map<String, Object>>> auditoriaPlanos() {
        List<Map<String, Object>> auditoria = oracleService.consultarAuditoriaPlanos();
        return ResponseEntity.ok(auditoria);
    }


    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro ao executar procedure")
    })
    public ResponseEntity<Map<String, Object>> gerarRelatorioConsumo(
            @RequestBody AnaliseNutricionalRequest request
    ) {
        logger.info("REST API: Gerar relatório de consumo - request={}", request);

        try {
            String relatorio = this.oracleService.formatarRelatorioNutricao(request.getUserId());
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("relatorio", relatorio);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de consumo: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("sucesso", false);
            error.put("mensagem", "Erro ao gerar relatório: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Executa análise completa (combina function e procedures)
     */
    @PostMapping("/analise-completa/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Executar Análise Completa",
        description = "Combina function e procedures PL/SQL para análise completa do usuário: " +
                     "calcula indicador de saúde, registra alertas e gera relatório"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Análise executada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro ao executar análise")
    })
    public ResponseEntity<AnaliseNutricionalResponse> executarAnaliseCompleta(
            @Parameter(description = "ID do usuário") @PathVariable String userId,
            @Parameter(description = "Dias de análise (padrão: 7)") 
            @RequestParam(required = false, defaultValue = "7") Integer diasAnalise
    ) {
        logger.info("REST API: Executar análise completa - userId={}, diasAnalise={}", userId, diasAnalise);

        try {
            Map<String, Object> resultado = oracleService.executarAnaliseCompleta(userId, diasAnalise);

            AnaliseNutricionalResponse response = new AnaliseNutricionalResponse();
            response.setSucesso((Boolean) resultado.get("sucesso"));
            response.setMensagem((String) resultado.get("mensagem"));
            response.setScoreSaude((Double) resultado.get("scoreSaude"));
            response.setClassificacao((String) resultado.get("classificacao"));
            response.setAlertasGerados((Integer) resultado.get("alertasGerados"));
            response.setRelatorio((String) resultado.get("relatorioResumido"));

            if (response.getRelatorio() != null) {
                response.setTamanhoRelatorio(response.getRelatorio().length());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro na análise completa: {}", e.getMessage(), e);
            AnaliseNutricionalResponse error = new AnaliseNutricionalResponse(false, 
                "Erro na análise: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtém estatísticas básicas do usuário
     */
    @GetMapping("/estatisticas/{userId}")
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Obter Estatísticas Básicas",
        description = "Retorna estatísticas básicas do usuário usando consulta SQL direta"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estatísticas obtidas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao obter estatísticas")
    })
    public ResponseEntity<Map<String, Object>> obterEstatisticas(
            @Parameter(description = "ID do usuário") @PathVariable String userId
    ) {
        logger.info("REST API: Obter estatísticas - userId={}", userId);

        try {
            Map<String, Object> estatisticas = oracleService.obterEstatisticasBasicas(userId);
            estatisticas.put("sucesso", true);
            estatisticas.put("userId", userId);

            return ResponseEntity.ok(estatisticas);

        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("sucesso", false);
            error.put("mensagem", "Erro ao obter estatísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Function: formatar_relatorio_nutricao
    @GetMapping("/relatorio-nutricao/{userId}")
    public ResponseEntity<String> relatorioNutricao(@PathVariable String userId) {
        String relatorio = this.oracleService.formatarRelatorioNutricao(userId);
        return ResponseEntity.ok(relatorio);
    }

    // Procedure: proc_gerar_relatorio_consumo
    @PostMapping("/relatorio-llm/{userId}")
    @Operation(summary = "Gerar relatório + resumo via LLM", description = "Gera relatório de consumo e pede à LLM um resumo executivo e recomendações")
    public ResponseEntity<Map<String, Object>> gerarRelatorioComLlm(
            @Parameter(description = "ID do usuário") @PathVariable String userId) {

        try {
            Map<String, Object> result = oracleService.gerarRelatorioConsumo(userId);
            String relatorio = (String) result.getOrDefault("relatorio", "");
            try {
                String llmSummary = llmService.summarizeReport(relatorio);
                result.put("llmSummary", llmSummary);
            } catch (Exception e) {
                // Se a LLM falhar, retorna relatório original e mensagem de erro parcial
                result.put("llmSummary", null);
                result.put("llmError", "Erro ao gerar resumo via LLM: " + e.getMessage());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("sucesso", false);
            error.put("mensagem", "Erro ao gerar relatório: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
