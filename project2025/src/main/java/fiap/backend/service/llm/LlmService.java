package fiap.backend.service.llm;

public interface LlmService {
    /**
     * Gera um resumo/enriquecimento do relatório bruto.
     * Implementações podem chamar OpenAI, Anthropic, etc.
     */
    String summarizeReport(String report) throws Exception;

        /**
     * Gera sugestões práticas para otimização de consumo a partir do relatório bruto.
     * Retorna texto com resumo executivo + bullets de ações.
     */
    String generateConsumptionOptimization(String report) throws Exception;

    /**
     * Gera recomendações de alertas e priorização a partir do contexto de alertas ou relatório.
     * Deve retornar texto com nível de severidade sugerido e ações recomendadas.
     */
    String generateAlertRecommendations(String alertContext) throws Exception;
}
