package fiap.backend.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço unificado para integração com uma LLM via HTTP.
 * Fornece métodos gerais (summarizeReport) e específicos de otimização/alertas.
 */
@Service
public class OpenAiLlmService implements LlmService, OptimizationLlmService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.model:gpt-4o-mini}")
    private String model;

    @Value("${llm.optimization.model:${llm.api.model:gpt-4o-mini}}")
    private String optimizationModel;

    @Value("${llm.request.timeout.seconds:30}")
    private int timeoutSeconds;

    @Override
    public String summarizeReport(String report) throws Exception {
        if (report == null || report.isBlank()) {
            return "";
        }
        // Prompt simples: peça resumo executivo e bullets de ações
        String prompt = "Resuma o relatório abaixo em um resumo executivo curto (máximo 180 palavras) e inclua 5 bullets de recomendações práticas:\n\n"
                + report;

        // Corpo compatível com Chat Completions API (OpenAI-like). Ajuste conforme provedor.
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.2);
        // Mensagens no formato chat com 'user'
        Map<String, String> msg = Map.of("role", "user", "content", prompt);
        body.put("messages", new Object[]{msg});
        body.put("max_tokens", 512);

        return callLlm(body);
    }

    // Novo: gerar otimização de consumo baseada em relatório
    @Override
    public String generateConsumptionOptimization(String report) throws Exception {
        if (report == null || report.isBlank()) {
            return "Relatório vazio — sem dados para gerar otimização.";
        }

        String prompt = """
                Você é um assistente nutricional que fornece um resumo executivo curto (máx 150 palavras) e
                6 recomendações práticas para otimizar consumo diário baseado no relatório abaixo.
                Cada recomendação deve ser acionável (ex.: substituir X por Y, aumentar proteína no café da manhã, reduzir snack à noite).
                Dê também, no final, 3 métricas que o usuário pode monitorar nas próximas 2 semanas.

                RELATÓRIO:
                """ + report;

        Map<String, Object> body = new HashMap<>();
        body.put("model", optimizationModel);
        body.put("temperature", 0.12);
        body.put("messages", new Object[]{Map.of("role", "user", "content", prompt)});
        body.put("max_tokens", 700);

        return callLlm(body);
    }

    // Novo: gerar recomendações e priorização de alertas
    @Override
    public String generateAlertRecommendations(String alertContext) throws Exception {
        if (alertContext == null || alertContext.isBlank()) {
            return "Contexto de alertas vazio — sem recomendações disponíveis.";
        }

        String prompt = """
                Você é um assistente que prioriza e traduz alertas nutricionais em ações concretas.
                Para cada alerta fornecido: 1) Classifique severidade (CRITICAL / HIGH / MEDIUM / LOW),
                2) Sugira 2 ações imediatas (táticas) e 1 ação de acompanhamento (estratégia),
                3) Indique se precisa notificar profissional (SIM/NÃO) e por que.
                                
                CONTEXTO/ALERTAS:
                """ + alertContext;

        Map<String, Object> body = new HashMap<>();
        body.put("model", optimizationModel);
        body.put("temperature", 0.0);
        body.put("messages", new Object[]{Map.of("role", "user", "content", prompt)});
        body.put("max_tokens", 500);

        return callLlm(body);
    }

    // Chamada genérica para LLM (chat completions style) - consolidada
    private String callLlm(Map<String, Object> body) throws Exception {
        String requestJson = objectMapper.writeValueAsString(body);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(Math.max(10, timeoutSeconds)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson));

        if (apiKey != null && !apiKey.isBlank()) {
            reqBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest request = reqBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            // Tenta extrair texto de acordo com OpenAI chat completions response
            JsonNode contentNode = null;
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode first = root.get("choices").get(0);
                if (first.has("message")) {
                    contentNode = first.get("message").get("content");
                } else if (first.has("text")) {
                    contentNode = first.get("text");
                }
            }
            if (contentNode != null) {
                return contentNode.asText().trim();
            } else {
                // Fallback: retorna body bruto se não encontrar campo esperado
                return response.body();
            }
        } else {
            throw new RuntimeException("LLM API error: HTTP " + response.statusCode() + " - " + response.body());
        }
    }
}
