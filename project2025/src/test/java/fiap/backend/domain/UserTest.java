package fiap.backend.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        UUID testId = UUID.randomUUID();
        user.setId(testId);
        user.setFirstName("Maria");
        user.setLastName("Silva");
        user.setEmail("maria@email.com");
        user.setPasswordHash("hash");
        user.setRolesByNames(Set.of("USER"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setProfilePhotoUrl("http://img.com/foto.png");
        user.setPhoneE164("11999999999");
        user.setAddressStreet("Rua Teste, 123");
        user.setIsActive(true);

        assertEquals(testId, user.getId());
        assertEquals("Maria", user.getFirstName());
        assertEquals("Silva", user.getLastName());
        assertEquals("maria@email.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertTrue(user.getRoleNames().contains("USER"));
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals("http://img.com/foto.png", user.getProfilePhotoUrl());
        assertEquals("11999999999", user.getPhoneE164());
        assertEquals("Rua Teste, 123", user.getAddressStreet());
        assertTrue(user.getIsActive());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User();
        UUID testId = UUID.randomUUID();
        user1.setId(testId);
        user1.setEmail("a@a.com");

        User user2 = new User();
        user2.setId(testId);
        user2.setEmail("a@a.com");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        User user = new User();
        UUID testId = UUID.randomUUID();
        user.setId(testId);
        user.setFirstName("Teste");
        user.setLastName("Silva");
        user.setEmail("teste@teste.com");
        user.setPhoneE164("123");
        user.setAddressStreet("Rua X");
        user.setProfilePhotoUrl("url");
        String str = user.toString();
        assertTrue(str.contains("Teste"));
        assertTrue(str.contains("Silva"));
        assertTrue(str.contains("teste@teste.com"));
        assertTrue(str.contains("123"));
        assertTrue(str.contains("Rua X"));
        assertTrue(str.contains("url"));
    }
}
