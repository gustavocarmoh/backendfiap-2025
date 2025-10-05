package fiap.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("NutriXpert API")
                                                .description("""
                                                                ## Sistema Completo de Análise Nutricional

                                                                A API NutriXpert oferece um sistema completo para:
                                                                - 🔐 Autenticação e autorização JWT
                                                                - 👤 Gestão de usuários e perfis
                                                                - 📊 Sistema de planos de assinatura
                                                                - 🥗 Planos nutricionais personalizados
                                                                - 💬 Sistema de chat integrado
                                                                - 👥 Rede de prestadores de serviços
                                                                - 🎛️ Dashboard administrativo

                                                                ### Como usar esta documentação:
                                                                1. **Endpoints públicos**: Não requerem autenticação
                                                                2. **Endpoints protegidos**: Requerem token JWT
                                                                3. **Para testar**: Use o botão "Authorize" e insira seu token
                                                                """)
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Equipe NutriXpert")
                                                                .email("dev@nutrixpert.com")
                                                                .url("https://nutrixpert.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8000")
                                                                .description("Ambiente de Desenvolvimento Local"),
                                                new Server()
                                                                .url("http://localhost:8000")
                                                                .description("Ambiente de Desenvolvimento"),
                                                new Server()
                                                                .url("https://api.nutrixpert.com")
                                                                .description("Ambiente de Produção")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Token JWT obtido através do endpoint POST /api/v1/auth/login. Formato: Bearer {token}")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                // .tags(List.of(
                // new Tag().name("Autenticação").description(
                // "Endpoints para login, registro e gerenciamento de tokens"),
                // new Tag().name("Usuários")
                // .description("Gestão de perfis e dados dos usuários"),
                // new Tag().name("Planos de Assinatura").description(
                // "Gestão dos planos disponíveis no sistema"),
                // new Tag().name("Assinaturas").description(
                // "Gerenciamento de assinaturas dos usuários"),
                // new Tag().name("Planos Nutricionais").description(
                // "Criação e gestão de planos alimentares personalizados"),
                // new Tag().name("Chat")
                // .description("Sistema de mensagens entre usuários"),
                // new Tag().name("Prestadores de Serviços")
                // .description("Rede de nutricionistas e profissionais"),
                // new Tag().name("Dashboard Administrativo")
                // .description("Métricas e gestão do sistema"),
                // new Tag().name("Documentação").description(
                // "Endpoints de documentação e status da aplicação")));
        }
}
