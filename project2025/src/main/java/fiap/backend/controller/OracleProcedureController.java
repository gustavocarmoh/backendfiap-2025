package fiap.backend.controller;

import fiap.backend.dto.AnaliseNutricionalRequest;
import fiap.backend.dto.AnaliseNutricionalResponse;
import fiap.backend.service.OracleStoredProcedureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para executar Functions e Procedures PL/SQL do Oracle
 * Demonstra integração completa Backend Java → Oracle Database
 */
@RestController
@RequestMapping("/api/v1/oracle")
@Tag(name = "Oracle PL/SQL", description = "Endpoints para execução de Functions e Procedures Oracle PL/SQL")
@SecurityRequirement(name = "bearer-jwt")
public class OracleProcedureController {

    private static final Logger logger = LoggerFactory.getLogger(OracleProcedureController.class);

    private final OracleStoredProcedureService oracleService;

    public OracleProcedureController(OracleStoredProcedureService oracleService) {
        this.oracleService = oracleService;
    }

    /**
     * Calcula o indicador de saúde do usuário
     */
    @GetMapping("/indicador-saude/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Calcular Indicador de Saúde",
        description = "Executa a function PL/SQL calcular_indicador_saude_usuario para obter score de saúde nutricional (0-100)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Indicador calculado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro ao executar function")
    })
    public ResponseEntity<Map<String, Object>> calcularIndicadorSaude(
            @Parameter(description = "ID do usuário") @PathVariable String userId,
            @Parameter(description = "Dias de análise (padrão: 30)") @RequestParam(required = false, defaultValue = "30") Integer diasAnalise
    ) {
        logger.info("REST API: Calcular indicador de saúde - userId={}, diasAnalise={}", userId, diasAnalise);

        try {
            Double score = oracleService.calcularIndicadorSaude(userId, diasAnalise);

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

    /**
     * Gera relatório formatado de nutrição
     */
    @GetMapping(value = "/relatorio-nutricao/{userId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Gerar Relatório de Nutrição",
        description = "Executa a function PL/SQL formatar_relatorio_nutricao para gerar relatório formatado em texto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso", 
                     content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro ao executar function")
    })
    public ResponseEntity<String> gerarRelatorioNutricao(
            @Parameter(description = "ID do usuário") @PathVariable String userId,
            @Parameter(description = "Data de início (formato: YYYY-MM-DD)") 
            @RequestParam(required = false) LocalDate dataInicio,
            @Parameter(description = "Data de fim (formato: YYYY-MM-DD)") 
            @RequestParam(required = false) LocalDate dataFim
    ) {
        logger.info("REST API: Gerar relatório de nutrição - userId={}, dataInicio={}, dataFim={}", 
                    userId, dataInicio, dataFim);

        try {
            // Valores padrão
            if (dataFim == null) {
                dataFim = LocalDate.now();
            }
            if (dataInicio == null) {
                dataInicio = dataFim.minusDays(7);
            }

            String relatorio = oracleService.formatarRelatorioNutricao(userId, dataInicio, dataFim);
            return ResponseEntity.ok(relatorio);

        } catch (Exception e) {
            logger.error("Erro ao gerar relatório de nutrição: {}", e.getMessage(), e);
            String errorMsg = "ERRO AO GERAR RELATÓRIO\n" +
                            "========================\n" +
                            "Mensagem: " + e.getMessage() + "\n";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    /**
     * Registra alertas nutricionais para o usuário
     */
    @PostMapping("/alertas-nutricionais/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Registrar Alertas Nutricionais",
        description = "Executa a procedure PL/SQL proc_registrar_alerta_nutricional para analisar e registrar alertas"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alertas registrados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro ao executar procedure")
    })
    public ResponseEntity<Map<String, Object>> registrarAlertasNutricionais(
            @Parameter(description = "ID do usuário") @PathVariable String userId,
            @Parameter(description = "Dias de análise (padrão: 7)") 
            @RequestParam(required = false, defaultValue = "7") Integer diasAnalise
    ) {
        logger.info("REST API: Registrar alertas nutricionais - userId={}, diasAnalise={}", userId, diasAnalise);

        try {
            Integer alertasGerados = oracleService.registrarAlertasNutricionais(userId, diasAnalise);

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

    /**
     * Gera relatório detalhado de consumo
     */
    @PostMapping("/relatorio-consumo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Gerar Relatório de Consumo",
        description = "Executa a procedure PL/SQL proc_gerar_relatorio_consumo para gerar relatório detalhado"
    )
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
            // Valores padrão
            LocalDate dataFim = request.getDataFim() != null ? request.getDataFim() : LocalDate.now();
            LocalDate dataInicio = request.getDataInicio() != null ? 
                                   request.getDataInicio() : dataFim.minusDays(30);

            Map<String, Object> resultado = oracleService.gerarRelatorioConsumo(
                request.getUserId(), dataInicio, dataFim
            );

            return ResponseEntity.ok(resultado);

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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
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
}
