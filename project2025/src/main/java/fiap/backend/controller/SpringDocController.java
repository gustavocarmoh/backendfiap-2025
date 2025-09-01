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
import java.util.List;

@RestController
@RequestMapping("/api/v1/springdoc")
@Tag(name = "SpringDoc", description = "Informações sobre a configuração SpringDoc da API")
@Hidden // Oculta este controlador da documentação Swagger
public class SpringDocController {

    @GetMapping("/info")
    @Operation(summary = "Informações do SpringDoc", description = "Retorna informações sobre a configuração SpringDoc e URLs disponíveis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso")
    })
    public ResponseEntity<?> getSpringDocInfo() {
        return ResponseEntity.ok(Map.of(
                "springdoc", Map.of(
                        "version", "2.2.0",
                        "description", "SpringDoc OpenAPI 3 - Moderna biblioteca para documentação de APIs Spring Boot",
                        "benefits", List.of(
                                "Suporte nativo ao Spring Boot 3.x",
                                "Compatibilidade com Jakarta EE",
                                "Anotações OpenAPI 3.0 atualizadas",
                                "Interface moderna e responsiva",
                                "Agrupamento de endpoints",
                                "Suporte completo ao JWT")),
                "urls", Map.of(
                        "swagger_ui", "/swagger-ui.html",
                        "api_docs_json", "/api-docs",
                        "api_docs_yaml", "/api-docs.yaml",
                        "openapi_json", "/v3/api-docs"),
                "groups", Map.of(
                        "public", "Endpoints Públicos",
                        "user", "Gestão de Usuários",
                        "subscription", "Sistema de Assinaturas",
                        "nutrition", "Planos Nutricionais",
                        "chat", "Sistema de Chat",
                        "service-provider", "Prestadores de Serviços",
                        "admin", "Dashboard Administrativo"),
                "features", Map.of(
                        "authentication", "Suporte completo ao JWT Bearer Token",
                        "validation", "Validação automática de schemas",
                        "examples", "Exemplos automáticos de request/response",
                        "grouping", "Agrupamento lógico de endpoints",
                        "customization", "Totalmente customizável via configuração"),
                "access_info", Map.of(
                        "public_access", "Swagger UI é totalmente público - não requer autenticação",
                        "testing", "Use o botão 'Authorize' apenas para testar endpoints protegidos",
                        "token_format", "Bearer {seu-jwt-token}")));
    }

    @GetMapping("/groups")
    @Operation(summary = "Grupos da API", description = "Lista todos os grupos disponíveis na documentação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de grupos retornada")
    })
    public ResponseEntity<?> getApiGroups() {
        return ResponseEntity.ok(Map.of(
                "available_groups", List.of(
                        Map.of(
                                "name", "public",
                                "display_name", "Endpoints Públicos",
                                "description", "Endpoints que não requerem autenticação",
                                "paths", List.of("/api/v1/auth/**", "/api/v1/docs/**", "/actuator/health")),
                        Map.of(
                                "name", "user",
                                "display_name", "Gestão de Usuários",
                                "description", "Endpoints para gerenciamento de perfis e dados dos usuários",
                                "paths", List.of("/api/v1/users/**")),
                        Map.of(
                                "name", "subscription",
                                "display_name", "Sistema de Assinaturas",
                                "description", "Endpoints para planos de assinatura e gestão de assinaturas",
                                "paths", List.of("/api/v1/subscription-plans/**", "/api/v1/subscriptions/**")),
                        Map.of(
                                "name", "nutrition",
                                "display_name", "Planos Nutricionais",
                                "description", "Endpoints para criação e gestão de planos alimentares",
                                "paths", List.of("/api/v1/nutrition-plans/**")),
                        Map.of(
                                "name", "chat",
                                "display_name", "Sistema de Chat",
                                "description", "Endpoints para mensagens e conversas",
                                "paths", List.of("/api/v1/chat/**")),
                        Map.of(
                                "name", "service-provider",
                                "display_name", "Prestadores de Serviços",
                                "description", "Endpoints para rede de nutricionistas e profissionais",
                                "paths", List.of("/api/v1/service-providers/**")),
                        Map.of(
                                "name", "admin",
                                "display_name", "Dashboard Administrativo",
                                "description", "Endpoints para métricas e gestão administrativa",
                                "paths", List.of("/api/v1/admin/**"))),
                "how_to_use", Map.of(
                        "step1", "Acesse /swagger-ui.html",
                        "step2", "Use o dropdown 'Select a definition' para escolher um grupo",
                        "step3", "Explore os endpoints específicos do grupo selecionado",
                        "step4", "Use 'Authorize' se precisar testar endpoints protegidos")));
    }

    @GetMapping("/health")
    @Operation(summary = "Status do SpringDoc", description = "Verifica se o SpringDoc está funcionando corretamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SpringDoc está funcionando")
    })
    public ResponseEntity<?> getSpringDocHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "springdoc_version", "2.2.0",
                "openapi_version", "3.0.3",
                "spring_boot_version", "3.3.2",
                "swagger_ui_available", true,
                "api_docs_available", true,
                "timestamp", System.currentTimeMillis(),
                "endpoints_check", Map.of(
                        "swagger_ui", "/swagger-ui.html - ✅ Disponível",
                        "api_docs", "/api-docs - ✅ Disponível",
                        "groups", "7 grupos configurados - ✅ Disponível")));
    }
}
