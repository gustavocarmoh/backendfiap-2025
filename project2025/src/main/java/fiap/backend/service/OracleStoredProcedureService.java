package fiap.backend.service;

import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço para execução de Functions e Procedures PL/SQL do Oracle
 * Demonstra integração Java com lógica de banco de dados Oracle
 */
@Service
@Transactional
public class OracleStoredProcedureService {

    private static final Logger logger = LoggerFactory.getLogger(OracleStoredProcedureService.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public OracleStoredProcedureService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Calcula o indicador de saúde do usuário usando a function PL/SQL
     * 
     * @param userId ID do usuário
     * @param diasAnalise Número de dias para análise (padrão: 30)
     * @return Score de saúde de 0 a 100
     */
    public Double calcularIndicadorSaude(String userId, Integer diasAnalise) {
        if (diasAnalise == null) {
            diasAnalise = 30;
        }

        logger.info("Calculando indicador de saúde para usuário: {} (últimos {} dias)", userId, diasAnalise);

        String sql = "SELECT calcular_indicador_saude_usuario(?, ?) FROM DUAL";

        try {
            Double score = jdbcTemplate.queryForObject(sql, Double.class, userId, diasAnalise);
            logger.info("Indicador de saúde calculado: {}/100", score);
            return score;
        } catch (Exception e) {
            logger.error("Erro ao calcular indicador de saúde: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao calcular indicador de saúde: " + e.getMessage(), e);
        }
    }

    /**
     * Formata o relatório de nutrição usando a function PL/SQL
     * 
     * @param userId ID do usuário
     * @param dataInicio Data de início do período
     * @param dataFim Data de fim do período
     * @return Relatório formatado em texto
     */
    public String formatarRelatorioNutricao(String userId, LocalDate dataInicio, LocalDate dataFim) {
        logger.info("Gerando relatório de nutrição para usuário: {} (período: {} a {})", 
                    userId, dataInicio, dataFim);

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT formatar_relatorio_nutricao(?, ?, ?) FROM DUAL";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                stmt.setDate(2, Date.valueOf(dataInicio));
                stmt.setDate(3, Date.valueOf(dataFim));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Clob clob = rs.getClob(1);
                        if (clob != null) {
                            String relatorio = clob.getSubString(1, (int) clob.length());
                            logger.info("Relatório gerado com sucesso ({} caracteres)", relatorio.length());
                            return relatorio;
                        }
                    }
                }
            }

            logger.warn("Relatório não gerado - sem dados");
            return "Relatório não disponível";

        } catch (SQLException e) {
            logger.error("Erro ao gerar relatório de nutrição: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar relatório de nutrição: " + e.getMessage(), e);
        }
    }

    /**
     * Registra alertas nutricionais usando a procedure PL/SQL
     * 
     * @param userId ID do usuário
     * @param diasAnalise Número de dias para análise
     * @return Número de alertas gerados
     */
    public Integer registrarAlertasNutricionais(String userId, Integer diasAnalise) {
        if (diasAnalise == null) {
            diasAnalise = 7;
        }

        logger.info("Registrando alertas nutricionais para usuário: {} (últimos {} dias)", userId, diasAnalise);

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call proc_registrar_alerta_nutricional(?, ?, ?)}")) {

            // Parâmetros IN
            stmt.setString(1, userId);
            stmt.setInt(2, diasAnalise);

            // Parâmetro OUT
            stmt.registerOutParameter(3, Types.INTEGER);

            // Executar procedure
            stmt.execute();

            // Obter resultado
            Integer alertasGerados = stmt.getInt(3);
            logger.info("Alertas nutricionais registrados: {}", alertasGerados);

            return alertasGerados;

        } catch (SQLException e) {
            logger.error("Erro ao registrar alertas nutricionais: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao registrar alertas nutricionais: " + e.getMessage(), e);
        }
    }

    /**
     * Gera relatório de consumo usando a procedure PL/SQL
     * 
     * @param userId ID do usuário
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Map com relatório e status de sucesso
     */
    public Map<String, Object> gerarRelatorioConsumo(String userId, LocalDate dataInicio, LocalDate dataFim) {
        logger.info("Gerando relatório de consumo para usuário: {} (período: {} a {})", 
                    userId, dataInicio, dataFim);

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call proc_gerar_relatorio_consumo(?, ?, ?, ?, ?)}")) {

            // Parâmetros IN
            stmt.setString(1, userId);
            stmt.setDate(2, Date.valueOf(dataInicio));
            stmt.setDate(3, Date.valueOf(dataFim));

            // Parâmetros OUT
            stmt.registerOutParameter(4, Types.CLOB);
            stmt.registerOutParameter(5, Types.INTEGER);

            // Executar procedure
            stmt.execute();

            // Obter resultados
            Clob clobRelatorio = stmt.getClob(4);
            Integer sucesso = stmt.getInt(5);

            String relatorio = "";
            if (clobRelatorio != null) {
                relatorio = clobRelatorio.getSubString(1, (int) clobRelatorio.length());
            }

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("sucesso", sucesso == 1);
            resultado.put("relatorio", relatorio);
            resultado.put("tamanho", relatorio.length());

            logger.info("Relatório de consumo gerado: sucesso={}, tamanho={}", 
                       sucesso == 1, relatorio.length());

            return resultado;

        } catch (SQLException e) {
            logger.error("Erro ao gerar relatório de consumo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar relatório de consumo: " + e.getMessage(), e);
        }
    }

    /**
     * Executa análise completa do usuário
     * Combina function de indicador de saúde e procedure de alertas
     * 
     * @param userId ID do usuário
     * @param diasAnalise Dias para análise
     * @return Map com resultados da análise
     */
    public Map<String, Object> executarAnaliseCompleta(String userId, Integer diasAnalise) {
        logger.info("Executando análise completa para usuário: {}", userId);

        Map<String, Object> resultado = new HashMap<>();

        try {
            // 1. Calcular indicador de saúde
            Double scoreSaude = calcularIndicadorSaude(userId, diasAnalise);
            resultado.put("scoreSaude", scoreSaude);

            // Classificar o score
            String classificacao;
            if (scoreSaude >= 85) {
                classificacao = "EXCELENTE";
            } else if (scoreSaude >= 70) {
                classificacao = "BOM";
            } else if (scoreSaude >= 50) {
                classificacao = "REGULAR";
            } else {
                classificacao = "PRECISA MELHORAR";
            }
            resultado.put("classificacao", classificacao);

            // 2. Registrar alertas nutricionais
            Integer alertasGerados = registrarAlertasNutricionais(userId, diasAnalise);
            resultado.put("alertasGerados", alertasGerados);

            // 3. Gerar relatório resumido
            LocalDate dataFim = LocalDate.now();
            LocalDate dataInicio = dataFim.minusDays(diasAnalise != null ? diasAnalise : 7);
            String relatorio = formatarRelatorioNutricao(userId, dataInicio, dataFim);
            resultado.put("relatorioResumido", relatorio);

            resultado.put("sucesso", true);
            resultado.put("mensagem", "Análise completa executada com sucesso");

            logger.info("Análise completa finalizada: score={}, alertas={}, classificacao={}", 
                       scoreSaude, alertasGerados, classificacao);

        } catch (Exception e) {
            logger.error("Erro na análise completa: {}", e.getMessage(), e);
            resultado.put("sucesso", false);
            resultado.put("mensagem", "Erro na análise: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Busca estatísticas básicas de um usuário usando SQL direto
     * Útil para validar dados antes de executar procedures
     * 
     * @param userId ID do usuário
     * @return Map com estatísticas básicas
     */
    public Map<String, Object> obterEstatisticasBasicas(String userId) {
        logger.info("Obtendo estatísticas básicas para usuário: {}", userId);

        String sql = """
            SELECT 
                COUNT(*) as total_planos,
                SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) as planos_completados,
                AVG(CASE WHEN is_completed = 1 THEN total_calories END) as media_calorias,
                MAX(plan_date) as ultimo_plano
            FROM nutrition_plans
            WHERE user_id = ?
            AND plan_date >= TRUNC(SYSDATE) - 30
            """;

        try {
            return jdbcTemplate.queryForMap(sql, userId);
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas básicas: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}
