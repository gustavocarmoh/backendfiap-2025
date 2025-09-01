package fiap.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testGettersAndSetters() {
        AuthResponse resp = new AuthResponse("aaaa");
        resp.setToken("abc123");
        assertEquals("abc123", resp.getToken());
    }

    @Test
    void testConstructor() {
        AuthResponse resp = new AuthResponse("xyz789");
        assertEquals("xyz789", resp.getToken());
    }
}
