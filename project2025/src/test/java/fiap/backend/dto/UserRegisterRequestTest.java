package fiap.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRegisterRequestTest {

    @Test
    void testGettersAndSetters() {
        UserRegisterRequest req = new UserRegisterRequest("", "", "");
        req.setName("João");
        req.setEmail("joao@email.com");
        req.setPassword("senha123");

        assertEquals("João", req.getName());
        assertEquals("joao@email.com", req.getEmail());
        assertEquals("senha123", req.getPassword());
    }

    @Test
    void testConstructor() {
        UserRegisterRequest req = new UserRegisterRequest("Ana", "ana@email.com", "senha456");
        assertEquals("Ana", req.getName());
        assertEquals("ana@email.com", req.getEmail());
        assertEquals("senha456", req.getPassword());
    }
}
