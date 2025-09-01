package fiap.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/docs")
@Tag(name = "Documentação", description = "APIs para acessar documentação e informações da aplicação")
@Hidden // Oculta este controlador da documentação Swagger
public class DocumentationController {

    @GetMapping
    @Operation(summary = "Informações da API", description = "Retorna informações básicas sobre a API e links úteis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso")
    })
    public ResponseEntity<?> getApiInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "NutriXpert API",
                "version", "1.0.0",
                "description", "Sistema completo de análise nutricional com sistema de subscrições",
                "swagger_ui", "/swagger-ui.html",
                "api_docs", "/api-docs",
                "health_check", "/actuator/health",
                "flyway_info", "/actuator/flyway",
                "endpoints", Map.of(
                        "auth", "/api/v1/auth",
                        "subscription_plans", "/api/v1/subscription-plans",
                        "subscriptions", "/api/v1/subscriptions",
                        "docs", "/api/v1/docs")));
    }

    @GetMapping("/swagger")
    @Operation(summary = "Acessar Swagger UI", description = "Retorna informações e links para acessar a documentação Swagger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações do Swagger retornadas"),
            @ApiResponse(responseCode = "302", description = "Redirecionamento para Swagger UI")
    })
    public ResponseEntity<?> getSwaggerInfo(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean redirect) {

        if (redirect) {
            // Redirecionar diretamente para Swagger UI
            return ResponseEntity.status(302)
                    .header("Location", "/swagger-ui.html")
                    .body(Map.of("message", "Redirecting to Swagger UI", "url", "/swagger-ui.html"));
        }

        // Retornar informações sobre o Swagger
        return ResponseEntity.ok(Map.of(
                "swagger", Map.of(
                        "ui_url", "/swagger-ui.html",
                        "api_docs_url", "/api-docs",
                        "api_docs_yaml", "/api-docs.yaml",
                        "openapi_json", "/v3/api-docs",
                        "description", "Interface interativa da documentação da API"),
                "endpoints", Map.of(
                        "swagger_ui", "/swagger-ui.html",
                        "openapi_spec", "/api-docs",
                        "redirect_to_ui", "/api/v1/docs/swagger?redirect=true"),
                "features", java.util.Arrays.asList(
                        "Documentação interativa",
                        "Teste de endpoints em tempo real",
                        "Especificação OpenAPI 3.0",
                        "Autenticação JWT integrada",
                        "Exemplos de request/response"),
                "instructions", Map.of(
                        "access", "Acesse /swagger-ui.html para usar a interface",
                        "auth", "Use o botão 'Authorize' para inserir seu JWT token",
                        "format", "Bearer {seu-token-jwt}",
                        "test", "Teste os endpoints diretamente na interface")));
    }

    @GetMapping("/health")
    @Operation(summary = "Status da aplicação", description = "Retorna o status de saúde da aplicação e suas dependências")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status da aplicação")
    })
    public ResponseEntity<?> getHealthStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "NutriXpert API",
                "timestamp", System.currentTimeMillis(),
                "checks", Map.of(
                        "database", "UP",
                        "flyway", "UP",
                        "auth_service", "UP",
                        "subscription_service", "UP"),
                "endpoints", Map.of(
                        "detailed_health", "/actuator/health",
                        "flyway_migrations", "/actuator/flyway")));
    }

    @GetMapping("/swagger-ui")
    @Operation(summary = "Interface Swagger UI", description = "Endpoint direto para acessar a interface Swagger UI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecionamento permanente para Swagger UI")
    })
    public ResponseEntity<?> swaggerUI() {
        return ResponseEntity.status(302)
                .header("Location", "/swagger-ui.html")
                .header("Cache-Control", "public, max-age=3600")
                .build();
    }

    @GetMapping("/api-spec")
    @Operation(summary = "Especificação OpenAPI", description = "Retorna a especificação OpenAPI da API em formato JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecionamento para especificação OpenAPI")
    })
    public ResponseEntity<?> getApiSpec() {
        return ResponseEntity.status(302)
                .header("Location", "/api-docs")
                .build();
    }
}
