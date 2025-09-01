package fiap.backend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionPlanResponseTest {

    private SubscriptionPlanResponse subscriptionPlanResponse;
    private UUID testId;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testDateTime = LocalDateTime.now();
        subscriptionPlanResponse = new SubscriptionPlanResponse();
    }

    @Test
    void testConstructorWithParameters() {
        // Given
        String name = "Premium Plan";
        BigDecimal price = BigDecimal.valueOf(29.99);
        Boolean isActive = true;

        // When
        SubscriptionPlanResponse response = new SubscriptionPlanResponse(
                testId, name, price, testDateTime, testDateTime, isActive);

        // Then
        assertEquals(testId, response.getId());
        assertEquals(name, response.getName());
        assertEquals(price, response.getPrice());
        assertEquals(testDateTime, response.getCreatedAt());
        assertEquals(testDateTime, response.getUpdatedAt());
        assertEquals(isActive, response.getIsActive());
    }

    @Test
    void testDefaultConstructor() {
        // When
        SubscriptionPlanResponse response = new SubscriptionPlanResponse();

        // Then
        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getPrice());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
        assertNull(response.getIsActive());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        String name = "Basic Plan";
        BigDecimal price = BigDecimal.valueOf(9.99);
        Boolean isActive = false;

        // When
        subscriptionPlanResponse.setId(testId);
        subscriptionPlanResponse.setName(name);
        subscriptionPlanResponse.setPrice(price);
        subscriptionPlanResponse.setCreatedAt(testDateTime);
        subscriptionPlanResponse.setUpdatedAt(testDateTime);
        subscriptionPlanResponse.setIsActive(isActive);

        // Then
        assertEquals(testId, subscriptionPlanResponse.getId());
        assertEquals(name, subscriptionPlanResponse.getName());
        assertEquals(price, subscriptionPlanResponse.getPrice());
        assertEquals(testDateTime, subscriptionPlanResponse.getCreatedAt());
        assertEquals(testDateTime, subscriptionPlanResponse.getUpdatedAt());
        assertEquals(isActive, subscriptionPlanResponse.getIsActive());
    }

    @Test
    void testAllFieldsNull() {
        // Given
        SubscriptionPlanResponse response = new SubscriptionPlanResponse();

        // When - setting all fields to null
        response.setId(null);
        response.setName(null);
        response.setPrice(null);
        response.setCreatedAt(null);
        response.setUpdatedAt(null);
        response.setIsActive(null);

        // Then
        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getPrice());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
        assertNull(response.getIsActive());
    }
}
