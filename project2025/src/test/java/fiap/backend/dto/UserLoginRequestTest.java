package fiap.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserLoginRequestTest {

    @Test
    void testGettersAndSetters() {
        UserLoginRequest req = new UserLoginRequest("", "");
        req.setEmail("login@email.com");
        req.setPassword("senha");

        assertEquals("login@email.com", req.getEmail());
        assertEquals("senha", req.getPassword());
    }

    @Test
    void testConstructor() {
        UserLoginRequest req = new UserLoginRequest("a@a.com", "123");
        assertEquals("a@a.com", req.getEmail());
        assertEquals("123", req.getPassword());
    }
}
