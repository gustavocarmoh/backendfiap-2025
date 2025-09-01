package fiap.backend.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("Todos os Endpoints da API")
                .pathsToMatch("/api/**", "/actuator/health")
                .pathsToExclude("/api/v1/docs/**", "/api/v1/springdoc/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("Autenticacao")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .displayName("Usuarios")
                .pathsToMatch("/api/v1/users/**", "/api/v1/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi subscriptionApi() {
        return GroupedOpenApi.builder()
                .group("subscription")
                .displayName("Subscricoes")
                .pathsToMatch("/api/v1/subscriptions/**", "/api/v1/subscription-plans/**")
                .build();
    }

    @Bean
    public GroupedOpenApi nutritionApi() {
        return GroupedOpenApi.builder()
                .group("nutrition")
                .displayName("Planos Nutricionais")
                .pathsToMatch("/api/nutrition-plans/**")
                .build();
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                .group("chat")
                .displayName("Sistema de Chat")
                .pathsToMatch("/api/chat/**")
                .build();
    }

    @Bean
    public GroupedOpenApi serviceProviderApi() {
        return GroupedOpenApi.builder()
                .group("service-provider")
                .displayName("Prestadores de Servicos")
                .pathsToMatch("/api/v1/service-providers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Dashboard Administrativo")
                .pathsToMatch("/api/v1/admin/**", "/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi photosApi() {
        return GroupedOpenApi.builder()
                .group("photos")
                .displayName("Fotos")
                .pathsToMatch("/api/photos/**", "/api/v1/user/photo/**")
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("Sistema")
                .pathsToMatch("/actuator/health")
                .build();
    }
}
