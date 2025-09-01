package fiap.backend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionPlanRequestTest {

    private SubscriptionPlanRequest subscriptionPlanRequest;

    @BeforeEach
    void setUp() {
        subscriptionPlanRequest = new SubscriptionPlanRequest();
    }

    @Test
    void testConstructorWithParameters() {
        // Given
        String name = "Premium Plan";
        BigDecimal price = BigDecimal.valueOf(29.99);

        // When
        SubscriptionPlanRequest request = new SubscriptionPlanRequest(name, price);

        // Then
        assertEquals(name, request.getName());
        assertEquals(price, request.getPrice());
    }

    @Test
    void testDefaultConstructor() {
        // When
        SubscriptionPlanRequest request = new SubscriptionPlanRequest();

        // Then
        assertNull(request.getName());
        assertNull(request.getPrice());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        String name = "Basic Plan";
        BigDecimal price = BigDecimal.valueOf(9.99);

        // When
        subscriptionPlanRequest.setName(name);
        subscriptionPlanRequest.setPrice(price);

        // Then
        assertEquals(name, subscriptionPlanRequest.getName());
        assertEquals(price, subscriptionPlanRequest.getPrice());
    }

    @Test
    void testNameValidation() {
        // Given
        subscriptionPlanRequest.setName("Valid Plan Name");

        // Then
        assertNotNull(subscriptionPlanRequest.getName());
        assertFalse(subscriptionPlanRequest.getName().isEmpty());
    }

    @Test
    void testPriceValidation() {
        // Given
        BigDecimal validPrice = BigDecimal.valueOf(19.99);
        subscriptionPlanRequest.setPrice(validPrice);

        // Then
        assertNotNull(subscriptionPlanRequest.getPrice());
        assertTrue(subscriptionPlanRequest.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }
}
