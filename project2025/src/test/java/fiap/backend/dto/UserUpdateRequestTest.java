package fiap.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserUpdateRequestTest {

    @Test
    void testGettersAndSetters() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("b@b.com");
        req.setTelefone("123456");
        req.setEndereco("Rua Y");
        req.setProfilePictureUrl("url");

        assertEquals("b@b.com", req.getEmail());
        assertEquals("123456", req.getTelefone());
        assertEquals("Rua Y", req.getEndereco());
        assertEquals("url", req.getProfilePictureUrl());
    }
}
