package fiap.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Mesma chave usada no AuthServiceImpl - em produção, use variável de ambiente
    private final String jwtSecret = "minha-super-chave-secreta-jwt-de-32-caracteres-segura-para-desenvolvimento-local";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                String activePlanName = (String) claims.get("activePlanName");
                String activePlanId = (String) claims.get("activePlanId");

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Adicionar informações do plano ativo ao contexto da requisição
                    if (activePlanName != null) {
                        request.setAttribute("activePlanName", activePlanName);
                        request.setAttribute("activePlanId", activePlanId);
                    }

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("JWT token validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/health");
    }
}
