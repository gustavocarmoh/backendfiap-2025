package fiap.backend.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SimpleIntegrationTest {

    @Test
    void contextLoads() {
        // Este teste simplesmente verifica se o contexto Spring carrega corretamente
    }
}
