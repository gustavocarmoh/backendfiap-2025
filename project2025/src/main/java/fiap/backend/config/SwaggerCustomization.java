package fiap.backend.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerCustomization {

    @Bean
    public OpenApiCustomizer customizeOpenApi() {
        return openApi -> {
            // Remove caminhos relacionados ao próprio Swagger da documentação
            openApi.getPaths().entrySet().removeIf(entry -> {
                String path = entry.getKey();
                return path.startsWith("/api/v1/docs") ||
                        path.startsWith("/api/v1/springdoc") ||
                        path.startsWith("/swagger-ui") ||
                        path.startsWith("/api-docs") ||
                        path.startsWith("/v3/api-docs");
            });

            // Personalizar informações adicionais
            if (openApi.getInfo() != null) {
                openApi.getInfo().setDescription(
                        openApi.getInfo().getDescription() +
                                "\n\n**📝 Nota:** Esta documentação exclui automaticamente endpoints relacionados à própria documentação do Swagger para manter o foco na API funcional.");
            }
        };
    }
}
