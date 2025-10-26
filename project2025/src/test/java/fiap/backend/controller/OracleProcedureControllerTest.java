package fiap.backend.controller;

import fiap.backend.dto.AnaliseNutricionalRequest;
import fiap.backend.dto.AnaliseNutricionalResponse;
import fiap.backend.service.OracleStoredProcedureService;
import fiap.backend.service.llm.LlmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OracleProcedureController - Testes Unitários")
class OracleProcedureControllerTest {

    @Mock
    private OracleStoredProcedureService oracleService;

    @Mock
    private LlmService llmService; // <--- adiciona mock do LLM para injeção

    @InjectMocks
    private OracleProcedureController controller;

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        // Setup comum se necessário
    }

    // ========== Testes: calcularIndicadorSaude ==========

    @Test
    @DisplayName("Deve calcular indicador de saúde com sucesso - EXCELENTE")
    void deveCalcularIndicadorSaudeExcelente() {
        // Arrange
        Double scoreEsperado = 90.0;
        when(oracleService.calcularIndicadorSaude(USER_ID, 30))
            .thenReturn(scoreEsperado);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("sucesso"));
        assertEquals(USER_ID, body.get("userId"));
        assertEquals(30, body.get("diasAnalise"));
        assertEquals(scoreEsperado, body.get("scoreSaude"));
        assertEquals("EXCELENTE", body.get("classificacao"));
        
        verify(oracleService, times(1)).calcularIndicadorSaude(USER_ID, 30);
    }

    @Test
    @DisplayName("Deve calcular indicador de saúde - BOM")
    void deveCalcularIndicadorSaudeBom() {
        // Arrange
        Double scoreEsperado = 75.0;
        when(oracleService.calcularIndicadorSaude(USER_ID, 30))
            .thenReturn(scoreEsperado);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        Map<String, Object> body = response.getBody();
        assertEquals("BOM", body.get("classificacao"));
    }

    @Test
    @DisplayName("Deve calcular indicador de saúde - REGULAR")
    void deveCalcularIndicadorSaudeRegular() {
        // Arrange
        Double scoreEsperado = 60.0;
        when(oracleService.calcularIndicadorSaude(USER_ID, 30))
            .thenReturn(scoreEsperado);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        Map<String, Object> body = response.getBody();
        assertEquals("REGULAR", body.get("classificacao"));
    }

    @Test
    @DisplayName("Deve calcular indicador de saúde - PRECISA MELHORAR")
    void deveCalcularIndicadorSaudePrecisaMelhorar() {
        // Arrange
        Double scoreEsperado = 40.0;
        when(oracleService.calcularIndicadorSaude(USER_ID, 30))
            .thenReturn(scoreEsperado);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        Map<String, Object> body = response.getBody();
        assertEquals("PRECISA MELHORAR", body.get("classificacao"));
    }

    @Test
    @DisplayName("Deve usar dias padrão quando não informado")
    void deveUsarDiasPadraoQuandoNaoInformado() {
        // Arrange
        when(oracleService.calcularIndicadorSaude(USER_ID, 30))
            .thenReturn(80.0);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        assertNotNull(response);
        verify(oracleService, times(1)).calcularIndicadorSaude(USER_ID, 30);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando service lança exceção")
    void deveRetornarErro500QuandoCalculoFalha() {
        // Arrange
        when(oracleService.calcularIndicadorSaude(anyString(), anyInt()))
            .thenThrow(new RuntimeException("Erro no banco de dados"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("sucesso"));
        assertTrue(body.get("mensagem").toString().contains("Erro ao calcular indicador"));
    }

    // ========== Testes: gerarRelatorioNutricao ==========

    @Test
    @DisplayName("Deve gerar relatório de nutrição com sucesso")
    void deveGerarRelatorioNutricaoComSucesso() {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        String relatorioEsperado = "RELATÓRIO DE NUTRIÇÃO\n===================\nDados do período...";
        
        when(oracleService.formatarRelatorioNutricao(eq(USER_ID), eq(dataInicio), eq(dataFim)))
            .thenReturn(relatorioEsperado);

        // Act
        ResponseEntity<String> response = controller.gerarRelatorioNutricao(USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(relatorioEsperado, response.getBody());
        verify(oracleService, times(1)).formatarRelatorioNutricao(eq(USER_ID), eq(dataInicio), eq(dataFim));
    }

    @Test
    @DisplayName("Deve usar datas padrão quando não informadas")
    void deveUsarDatasPadraoNoRelatorio() {
        // Arrange
        String relatorio = "Relatório padrão";
        when(oracleService.formatarRelatorioNutricao(eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(relatorio);

        // Act
        ResponseEntity<String> response = controller.gerarRelatorioNutricao(USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(oracleService, times(1))
            .formatarRelatorioNutricao(eq(USER_ID), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar erro quando geração de relatório falha")
    void deveRetornarErroQuandoRelatorioFalha() {
        // Arrange
        when(oracleService.formatarRelatorioNutricao(anyString(), any(), any()))
            .thenThrow(new RuntimeException("Erro ao gerar CLOB"));

        // Act
        ResponseEntity<String> response = controller.gerarRelatorioNutricao(USER_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("ERRO AO GERAR RELATÓRIO"));
    }

    // ========== Testes: registrarAlertasNutricionais ==========

    @Test
    @DisplayName("Deve registrar alertas nutricionais com sucesso")
    void deveRegistrarAlertasComSucesso() {
        // Arrange
        Integer alertasEsperados = 3;
        when(oracleService.registrarAlertasNutricionais(USER_ID, 7))
            .thenReturn(alertasEsperados);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            controller.registrarAlertasNutricionais(USER_ID, 7);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("sucesso"));
        assertEquals(alertasEsperados, body.get("alertasGerados"));
        assertTrue(body.get("mensagem").toString().contains("3 alerta(s)"));
        
        verify(oracleService, times(1)).registrarAlertasNutricionais(USER_ID, 7);
    }

    @Test
    @DisplayName("Deve registrar zero alertas quando nenhum problema encontrado")
    void deveRegistrarZeroAlertasQuandoSemProblemas() {
        // Arrange
        when(oracleService.registrarAlertasNutricionais(USER_ID, 7))
            .thenReturn(0);

        // Act
        ResponseEntity<Map<String, Object>> response = 
            controller.registrarAlertasNutricionais(USER_ID, 7);

        // Assert
        Map<String, Object> body = response.getBody();
        assertEquals(0, body.get("alertasGerados"));
    }

    @Test
    @DisplayName("Deve usar dias padrão para alertas quando não informado")
    void deveUsarDiasPadraoParaAlertas() {
        // Arrange
        when(oracleService.registrarAlertasNutricionais(USER_ID, 7))
            .thenReturn(2);

        // Act
        controller.registrarAlertasNutricionais(USER_ID, 7);

        // Assert
        verify(oracleService, times(1)).registrarAlertasNutricionais(USER_ID, 7);
    }

    @Test
    @DisplayName("Deve retornar erro quando registro de alertas falha")
    void deveRetornarErroQuandoAlertasFalha() {
        // Arrange
        when(oracleService.registrarAlertasNutricionais(anyString(), anyInt()))
            .thenThrow(new RuntimeException("Erro no CURSOR"));

        // Act
        ResponseEntity<Map<String, Object>> response = 
            controller.registrarAlertasNutricionais(USER_ID, 7);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("sucesso"));
    }

    // ========== Testes: gerarRelatorioConsumo ==========

    @Test
    @DisplayName("Deve gerar relatório de consumo com sucesso")
    void deveGerarRelatorioConsumoComSucesso() {
        // Arrange
        AnaliseNutricionalRequest request = new AnaliseNutricionalRequest();
        request.setUserId(USER_ID);
        request.setDataInicio(LocalDate.of(2025, 9, 1));
        request.setDataFim(LocalDate.of(2025, 9, 30));

        Map<String, Object> resultadoEsperado = new HashMap<>();
        resultadoEsperado.put("relatorio", "Relatório detalhado");
        resultadoEsperado.put("totalDias", 30);
        
        when(oracleService.gerarRelatorioConsumo(eq(USER_ID), any(), any()))
            .thenReturn(resultadoEsperado);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.gerarRelatorioConsumo(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultadoEsperado, response.getBody());
        verify(oracleService, times(1))
            .gerarRelatorioConsumo(eq(USER_ID), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve usar datas padrão no relatório de consumo")
    void deveUsarDatasPadraoNoRelatorioConsumo() {
        // Arrange
        AnaliseNutricionalRequest request = new AnaliseNutricionalRequest();
        request.setUserId(USER_ID);
        
        Map<String, Object> resultado = new HashMap<>();
        when(oracleService.gerarRelatorioConsumo(eq(USER_ID), any(), any()))
            .thenReturn(resultado);

        // Act
        controller.gerarRelatorioConsumo(request);

        // Assert
        verify(oracleService, times(1))
            .gerarRelatorioConsumo(eq(USER_ID), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Deve retornar erro quando relatório de consumo falha")
    void deveRetornarErroQuandoRelatorioConsumoFalha() {
        // Arrange
        AnaliseNutricionalRequest request = new AnaliseNutricionalRequest();
        request.setUserId(USER_ID);
        
        when(oracleService.gerarRelatorioConsumo(anyString(), any(), any()))
            .thenThrow(new RuntimeException("Erro na procedure"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.gerarRelatorioConsumo(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("sucesso"));
    }

    // ========== Testes: executarAnaliseCompleta ==========

    @Test
    @DisplayName("Deve executar análise completa com sucesso")
    void deveExecutarAnaliseCompletaComSucesso() {
        // Arrange
        Map<String, Object> resultadoEsperado = new HashMap<>();
        resultadoEsperado.put("sucesso", true);
        resultadoEsperado.put("mensagem", "Análise concluída");
        resultadoEsperado.put("scoreSaude", 85.0);
        resultadoEsperado.put("classificacao", "EXCELENTE");
        resultadoEsperado.put("alertasGerados", 2);
        resultadoEsperado.put("relatorioResumido", "Resumo da análise");
        
        when(oracleService.executarAnaliseCompleta(USER_ID, 7))
            .thenReturn(resultadoEsperado);

        // Act
        ResponseEntity<AnaliseNutricionalResponse> response = 
            controller.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnaliseNutricionalResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getSucesso());
        assertEquals(85.0, body.getScoreSaude());
        assertEquals("EXCELENTE", body.getClassificacao());
        assertEquals(2, body.getAlertasGerados());
        assertNotNull(body.getRelatorio());
        assertTrue(body.getTamanhoRelatorio() > 0);
        
        verify(oracleService, times(1)).executarAnaliseCompleta(USER_ID, 7);
    }

    @Test
    @DisplayName("Deve usar dias padrão na análise completa")
    void deveUsarDiasPadraoNaAnaliseCompleta() {
        // Arrange
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("sucesso", true);
        resultado.put("scoreSaude", 70.0);
        resultado.put("classificacao", "BOM");
        resultado.put("alertasGerados", 0);
        
        when(oracleService.executarAnaliseCompleta(USER_ID, 7))
            .thenReturn(resultado);

        // Act
        controller.executarAnaliseCompleta(USER_ID, null);

        // Assert
        verify(oracleService, times(1)).executarAnaliseCompleta(USER_ID, 7);
    }

    @Test
    @DisplayName("Deve retornar erro quando análise completa falha")
    void deveRetornarErroQuandoAnaliseCompletaFalha() {
        // Arrange
        when(oracleService.executarAnaliseCompleta(anyString(), anyInt()))
            .thenThrow(new RuntimeException("Erro geral"));

        // Act
        ResponseEntity<AnaliseNutricionalResponse> response = 
            controller.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        AnaliseNutricionalResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.getSucesso());
        assertTrue(body.getMensagem().contains("Erro na análise"));
    }

    @Test
    @DisplayName("Deve tratar relatório nulo na análise completa")
    void deveTratarRelatorioNuloNaAnaliseCompleta() {
        // Arrange
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("sucesso", true);
        resultado.put("scoreSaude", 70.0);
        resultado.put("classificacao", "BOM");
        resultado.put("alertasGerados", 0);
        resultado.put("relatorioResumido", null); // CLOB nulo
        
        when(oracleService.executarAnaliseCompleta(USER_ID, 7))
            .thenReturn(resultado);

        // Act
        ResponseEntity<AnaliseNutricionalResponse> response = 
            controller.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        AnaliseNutricionalResponse body = response.getBody();
        assertNull(body.getRelatorio());
        assertNull(body.getTamanhoRelatorio());
    }

    // ========== Testes: obterEstatisticas ==========

    @Test
    @DisplayName("Deve obter estatísticas básicas com sucesso")
    void deveObterEstatisticasComSucesso() {
        // Arrange
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("totalPlanos", 15);
        estatisticas.put("planosCompletos", 10);
        estatisticas.put("mediaCalorias", 2000.0);
        
        when(oracleService.obterEstatisticasBasicas(USER_ID))
            .thenReturn(estatisticas);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.obterEstatisticas(USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("sucesso"));
        assertEquals(USER_ID, body.get("userId"));
        assertEquals(15, body.get("totalPlanos"));
        
        verify(oracleService, times(1)).obterEstatisticasBasicas(USER_ID);
    }

    @Test
    @DisplayName("Deve retornar erro quando obtenção de estatísticas falha")
    void deveRetornarErroQuandoEstatisticasFalha() {
        // Arrange
        when(oracleService.obterEstatisticasBasicas(anyString()))
            .thenThrow(new RuntimeException("Erro SQL"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.obterEstatisticas(USER_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("sucesso"));
        assertTrue(body.get("mensagem").toString().contains("Erro ao obter estatísticas"));
    }

    // ========== Testes de Edge Cases ==========

    @Test
    @DisplayName("Deve lidar com userId vazio")
    void deveLidarComUserIdVazio() {
        // Arrange
        when(oracleService.calcularIndicadorSaude("", 30))
            .thenThrow(new IllegalArgumentException("UserId não pode ser vazio"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude("", 30);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve lidar com dias de análise negativo")
    void deveLidarComDiasNegativo() {
        // Arrange
        when(oracleService.calcularIndicadorSaude(USER_ID, -5))
            .thenThrow(new IllegalArgumentException("Dias deve ser positivo"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.calcularIndicadorSaude(USER_ID, -5);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
