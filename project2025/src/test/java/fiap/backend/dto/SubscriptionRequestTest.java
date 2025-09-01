package fiap.backend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionRequestTest {

    private SubscriptionRequest subscriptionRequest;
    private UUID testPlanId;

    @BeforeEach
    void setUp() {
        testPlanId = UUID.randomUUID();
        subscriptionRequest = new SubscriptionRequest();
    }

    @Test
    void testConstructorWithParameters() {
        // When
        SubscriptionRequest request = new SubscriptionRequest(testPlanId);

        // Then
        assertEquals(testPlanId, request.getPlanId());
    }

    @Test
    void testDefaultConstructor() {
        // When
        SubscriptionRequest request = new SubscriptionRequest();

        // Then
        assertNull(request.getPlanId());
    }

    @Test
    void testSettersAndGetters() {
        // When
        subscriptionRequest.setPlanId(testPlanId);

        // Then
        assertEquals(testPlanId, subscriptionRequest.getPlanId());
    }

    @Test
    void testPlanIdValidation() {
        // Given
        subscriptionRequest.setPlanId(testPlanId);

        // Then
        assertNotNull(subscriptionRequest.getPlanId());
    }

    @Test
    void testNullPlanId() {
        // When
        subscriptionRequest.setPlanId(null);

        // Then
        assertNull(subscriptionRequest.getPlanId());
    }
}
