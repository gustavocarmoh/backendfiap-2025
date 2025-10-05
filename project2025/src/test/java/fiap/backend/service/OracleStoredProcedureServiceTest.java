package fiap.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OracleStoredProcedureService - Testes Unitários")
class OracleStoredProcedureServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private CallableStatement callableStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Clob clob;

    private OracleStoredProcedureService service;

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        service = new OracleStoredProcedureService(dataSource, jdbcTemplate);
    }

    // ========== Testes: calcularIndicadorSaude ==========

    @Test
    @DisplayName("Deve calcular indicador de saúde com sucesso")
    void deveCalcularIndicadorSaudeComSucesso() {
        // Arrange
        Double scoreEsperado = 85.5;
        String sql = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        
        when(jdbcTemplate.queryForObject(sql, Double.class, USER_ID, 30))
            .thenReturn(scoreEsperado);

        // Act
        Double resultado = service.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        assertNotNull(resultado);
        assertEquals(scoreEsperado, resultado);
        verify(jdbcTemplate, times(1)).queryForObject(sql, Double.class, USER_ID, 30);
    }

    @Test
    @DisplayName("Deve usar dias padrão 30 quando nulo")
    void deveUsarDiasPadraoQuandoNulo() {
        // Arrange
        String sql = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        when(jdbcTemplate.queryForObject(sql, Double.class, USER_ID, 30))
            .thenReturn(75.0);

        // Act
        service.calcularIndicadorSaude(USER_ID, null);

        // Assert
        verify(jdbcTemplate, times(1)).queryForObject(sql, Double.class, USER_ID, 30);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando falha ao calcular indicador")
    void deveLancarExcecaoQuandoFalhaCalculo() {
        // Arrange
        String sql = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        when(jdbcTemplate.queryForObject(sql, Double.class, USER_ID, 30))
            .thenThrow(new DataAccessException("Erro no banco") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> service.calcularIndicadorSaude(USER_ID, 30));
        
        assertTrue(exception.getMessage().contains("Erro ao calcular indicador de saúde"));
    }

    @Test
    @DisplayName("Deve retornar score zero para usuário sem dados")
    void deveRetornarScoreZeroParaUsuarioSemDados() {
        // Arrange
        String sql = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        when(jdbcTemplate.queryForObject(sql, Double.class, USER_ID, 30))
            .thenReturn(0.0);

        // Act
        Double resultado = service.calcularIndicadorSaude(USER_ID, 30);

        // Assert
        assertEquals(0.0, resultado);
    }

    // ========== Testes: formatarRelatorioNutricao ==========

    @Test
    @DisplayName("Deve formatar relatório de nutrição com sucesso")
    void deveFormatarRelatorioComSucesso() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        String relatorioEsperado = "RELATÓRIO DE NUTRIÇÃO\nDados completos...";
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getClob(1)).thenReturn(clob);
        when(clob.length()).thenReturn((long) relatorioEsperado.length());
        when(clob.getSubString(1, relatorioEsperado.length())).thenReturn(relatorioEsperado);

        // Act
        String resultado = service.formatarRelatorioNutricao(USER_ID, dataInicio, dataFim);

        // Assert
        assertEquals(relatorioEsperado, resultado);
        verify(preparedStatement).setString(1, USER_ID);
        verify(preparedStatement).setDate(2, Date.valueOf(dataInicio));
        verify(preparedStatement).setDate(3, Date.valueOf(dataFim));
    }

    @Test
    @DisplayName("Deve retornar mensagem padrão quando CLOB é nulo")
    void deveRetornarMensagemPadraoQuandoClobNulo() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getClob(1)).thenReturn(null);

        // Act
        String resultado = service.formatarRelatorioNutricao(USER_ID, dataInicio, dataFim);

        // Assert
        assertEquals("Relatório não disponível", resultado);
    }

    @Test
    @DisplayName("Deve retornar mensagem padrão quando sem resultados")
    void deveRetornarMensagemPadraoQuandoSemResultados() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        String resultado = service.formatarRelatorioNutricao(USER_ID, dataInicio, dataFim);

        // Assert
        assertEquals("Relatório não disponível", resultado);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando SQLException ocorre no relatório")
    void deveLancarExcecaoQuandoSQLExceptionNoRelatorio() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        
        when(dataSource.getConnection()).thenThrow(new SQLException("Erro de conexão"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> service.formatarRelatorioNutricao(USER_ID, dataInicio, dataFim));
        
        assertTrue(exception.getMessage().contains("Erro ao gerar relatório de nutrição"));
    }

    // ========== Testes: registrarAlertasNutricionais ==========

    @Test
    @DisplayName("Deve registrar alertas nutricionais com sucesso")
    void deveRegistrarAlertasComSucesso() throws SQLException {
        // Arrange
        Integer alertasEsperados = 5;
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getInt(3)).thenReturn(alertasEsperados);

        // Act
        Integer resultado = service.registrarAlertasNutricionais(USER_ID, 7);

        // Assert
        assertEquals(alertasEsperados, resultado);
        verify(callableStatement).setString(1, USER_ID);
        verify(callableStatement).setInt(2, 7);
        verify(callableStatement).registerOutParameter(3, Types.INTEGER);
        verify(callableStatement).execute();
    }

    @Test
    @DisplayName("Deve usar dias padrão 7 quando nulo para alertas")
    void deveUsarDiasPadrao7QuandoNulo() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getInt(3)).thenReturn(3);

        // Act
        service.registrarAlertasNutricionais(USER_ID, null);

        // Assert
        verify(callableStatement).setInt(2, 7);
    }

    @Test
    @DisplayName("Deve retornar zero alertas quando tudo está normal")
    void deveRetornarZeroAlertasQuandoTudoNormal() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getInt(3)).thenReturn(0);

        // Act
        Integer resultado = service.registrarAlertasNutricionais(USER_ID, 7);

        // Assert
        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando SQLException na procedure")
    void deveLancarExcecaoQuandoSQLExceptionNaProcedure() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenThrow(new SQLException("Erro na procedure"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> service.registrarAlertasNutricionais(USER_ID, 7));
        
        assertTrue(exception.getMessage().contains("Erro ao registrar alertas nutricionais"));
    }

    // ========== Testes: gerarRelatorioConsumo ==========

    @Test
    @DisplayName("Deve gerar relatório de consumo com sucesso")
    void deveGerarRelatorioConsumoComSucesso() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        String relatorioTexto = "Relatório detalhado de consumo...";
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getClob(4)).thenReturn(clob);
        when(callableStatement.getInt(5)).thenReturn(1); // sucesso
        when(clob.length()).thenReturn((long) relatorioTexto.length());
        when(clob.getSubString(1, relatorioTexto.length())).thenReturn(relatorioTexto);

        // Act
        Map<String, Object> resultado = service.gerarRelatorioConsumo(USER_ID, dataInicio, dataFim);

        // Assert
        assertNotNull(resultado);
        assertTrue((Boolean) resultado.get("sucesso"));
        assertEquals(relatorioTexto, resultado.get("relatorio"));
        assertEquals(relatorioTexto.length(), resultado.get("tamanho"));
        
        verify(callableStatement).setString(1, USER_ID);
        verify(callableStatement).setDate(2, Date.valueOf(dataInicio));
        verify(callableStatement).setDate(3, Date.valueOf(dataFim));
        verify(callableStatement).registerOutParameter(4, Types.CLOB);
        verify(callableStatement).registerOutParameter(5, Types.INTEGER);
    }

    @Test
    @DisplayName("Deve retornar string vazia quando CLOB é nulo no relatório de consumo")
    void deveRetornarStringVaziaQuandoClobNuloNoConsumo() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getClob(4)).thenReturn(null);
        when(callableStatement.getInt(5)).thenReturn(1);

        // Act
        Map<String, Object> resultado = service.gerarRelatorioConsumo(USER_ID, dataInicio, dataFim);

        // Assert
        assertEquals("", resultado.get("relatorio"));
        assertEquals(0, resultado.get("tamanho"));
    }

    @Test
    @DisplayName("Deve retornar sucesso falso quando procedure falha")
    void deveRetornarSucessoFalsoQuandoProcedureFalha() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getClob(4)).thenReturn(clob);
        when(callableStatement.getInt(5)).thenReturn(0); // falha
        when(clob.length()).thenReturn(0L);
        when(clob.getSubString(1, 0)).thenReturn("");

        // Act
        Map<String, Object> resultado = service.gerarRelatorioConsumo(USER_ID, dataInicio, dataFim);

        // Assert
        assertFalse((Boolean) resultado.get("sucesso"));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando SQLException no relatório de consumo")
    void deveLancarExcecaoQuandoSQLExceptionNoRelatorioConsumo() throws SQLException {
        // Arrange
        LocalDate dataInicio = LocalDate.of(2025, 9, 1);
        LocalDate dataFim = LocalDate.of(2025, 9, 30);
        
        when(dataSource.getConnection()).thenThrow(new SQLException("Erro de banco"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> service.gerarRelatorioConsumo(USER_ID, dataInicio, dataFim));
        
        assertTrue(exception.getMessage().contains("Erro ao gerar relatório de consumo"));
    }

    // ========== Testes: executarAnaliseCompleta ==========

    @Test
    @DisplayName("Deve executar análise completa com sucesso")
    void deveExecutarAnaliseCompletaComSucesso() throws SQLException {
        // Arrange
        Double scoreEsperado = 88.0;
        Integer alertasEsperados = 2;
        String relatorioEsperado = "Relatório completo";
        
        // Mock calcularIndicadorSaude
        String sqlIndicador = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        when(jdbcTemplate.queryForObject(sqlIndicador, Double.class, USER_ID, 7))
            .thenReturn(scoreEsperado);
        
        // Mock registrarAlertasNutricionais
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getInt(3)).thenReturn(alertasEsperados);
        
        // Mock formatarRelatorioNutricao
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getClob(1)).thenReturn(clob);
        when(clob.length()).thenReturn((long) relatorioEsperado.length());
        when(clob.getSubString(1, relatorioEsperado.length())).thenReturn(relatorioEsperado);

        // Act
        Map<String, Object> resultado = service.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertNotNull(resultado);
        assertTrue((Boolean) resultado.get("sucesso"));
        assertEquals(scoreEsperado, resultado.get("scoreSaude"));
        assertEquals("EXCELENTE", resultado.get("classificacao"));
        assertEquals(alertasEsperados, resultado.get("alertasGerados"));
        assertEquals(relatorioEsperado, resultado.get("relatorioResumido"));
    }

    @Test
    @DisplayName("Deve classificar corretamente - BOM")
    void deveClassificarComoBom() throws SQLException {
        // Arrange
        setupMocksParaAnaliseCompleta(75.0, 1, "Relatório");

        // Act
        Map<String, Object> resultado = service.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertEquals("BOM", resultado.get("classificacao"));
    }

    @Test
    @DisplayName("Deve classificar corretamente - REGULAR")
    void deveClassificarComoRegular() throws SQLException {
        // Arrange
        setupMocksParaAnaliseCompleta(60.0, 3, "Relatório");

        // Act
        Map<String, Object> resultado = service.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertEquals("REGULAR", resultado.get("classificacao"));
    }

    @Test
    @DisplayName("Deve classificar corretamente - PRECISA MELHORAR")
    void deveClassificarComoPrecisaMelhorar() throws SQLException {
        // Arrange
        setupMocksParaAnaliseCompleta(40.0, 5, "Relatório");

        // Act
        Map<String, Object> resultado = service.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertEquals("PRECISA MELHORAR", resultado.get("classificacao"));
    }

    @Test
    @DisplayName("Deve retornar sucesso falso quando análise completa falha")
    void deveRetornarSucessoFalsoQuandoAnaliseFalha() {
        // Arrange
        String sqlIndicador = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        when(jdbcTemplate.queryForObject(sqlIndicador, Double.class, USER_ID, 7))
            .thenThrow(new DataAccessException("Erro") {});

        // Act
        Map<String, Object> resultado = service.executarAnaliseCompleta(USER_ID, 7);

        // Assert
        assertFalse((Boolean) resultado.get("sucesso"));
        assertTrue(resultado.get("mensagem").toString().contains("Erro na análise"));
    }

    // ========== Testes: obterEstatisticasBasicas ==========

    @Test
    @DisplayName("Deve obter estatísticas básicas com sucesso")
    void deveObterEstatisticasBasicasComSucesso() {
        // Arrange
        Map<String, Object> estatisticasEsperadas = new HashMap<>();
        estatisticasEsperadas.put("total_planos", 20);
        estatisticasEsperadas.put("planos_completados", 15);
        estatisticasEsperadas.put("media_calorias", 2000.0);
        
        when(jdbcTemplate.queryForMap(anyString(), eq(USER_ID)))
            .thenReturn(estatisticasEsperadas);

        // Act
        Map<String, Object> resultado = service.obterEstatisticasBasicas(USER_ID);

        // Assert
        assertNotNull(resultado);
        assertEquals(20, resultado.get("total_planos"));
        assertEquals(15, resultado.get("planos_completados"));
        assertEquals(2000.0, resultado.get("media_calorias"));
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando erro ao obter estatísticas")
    void deveRetornarMapaVazioQuandoErro() {
        // Arrange
        when(jdbcTemplate.queryForMap(anyString(), eq(USER_ID)))
            .thenThrow(new DataAccessException("Erro SQL") {});

        // Act
        Map<String, Object> resultado = service.obterEstatisticasBasicas(USER_ID);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ========== Métodos Helper ==========

    private void setupMocksParaAnaliseCompleta(Double score, Integer alertas, String relatorio) throws SQLException {
        String sqlIndicador = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";
        when(jdbcTemplate.queryForObject(sqlIndicador, Double.class, USER_ID, 7))
            .thenReturn(score);
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.getInt(3)).thenReturn(alertas);
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getClob(1)).thenReturn(clob);
        when(clob.length()).thenReturn((long) relatorio.length());
        when(clob.getSubString(1, relatorio.length())).thenReturn(relatorio);
    }
}
