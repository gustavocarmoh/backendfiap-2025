package fiap.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/health")
                        .permitAll()

                        // Swagger UI e documentação - acesso público
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                                "/api-docs.yaml")
                        .permitAll()

                        // SpringDoc - recursos estáticos e webjars
                        .requestMatchers("/webjars/**", "/swagger-resources/**", "/swagger-resources")
                        .permitAll()

                        // Endpoints de documentação - acesso público
                        .requestMatchers("/api/v1/docs", "/api/v1/docs/**")
                        .permitAll()

                        // Actuator health check - acesso público
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/flyway")
                        .permitAll()

                        // Endpoints que requerem role ADMIN
                        .requestMatchers("/api/v1/users/promote").hasRole("ADMIN")

                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
