package fiap.backend.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fiap.backend.dto.AuthResponse;
import fiap.backend.dto.UserLoginRequest;
import fiap.backend.service.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@ActiveProfiles("dev")
public class AuthIntegrationTest {

    @Autowired
    private AuthServiceImpl authService;

    @Test
    public void testRealLogin() {
        try {
            System.out.println("=== TESTE DE LOGIN REAL ===");

            UserLoginRequest loginRequest = new UserLoginRequest("admin@nutrixpert.local", "admin123");

            AuthResponse response = authService.login(loginRequest);

            System.out.println("✅ LOGIN BEM-SUCEDIDO!");
            System.out.println("Token JWT: " + response.getToken().substring(0, 50) + "...");
            System.out.println("Tamanho do token: " + response.getToken().length());

        } catch (Exception e) {
            System.err.println("❌ ERRO NO LOGIN: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
