package fiap.backend.dto;

import fiap.backend.domain.Subscription.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionStatusUpdateRequestTest {

    private SubscriptionStatusUpdateRequest statusUpdateRequest;

    @BeforeEach
    void setUp() {
        statusUpdateRequest = new SubscriptionStatusUpdateRequest();
    }

    @Test
    void testConstructorWithParameters() {
        // Given
        SubscriptionStatus status = SubscriptionStatus.APPROVED;

        // When
        SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest(status);

        // Then
        assertEquals(status, request.getStatus());
    }

    @Test
    void testDefaultConstructor() {
        // When
        SubscriptionStatusUpdateRequest request = new SubscriptionStatusUpdateRequest();

        // Then
        assertNull(request.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        SubscriptionStatus status = SubscriptionStatus.REJECTED;

        // When
        statusUpdateRequest.setStatus(status);

        // Then
        assertEquals(status, statusUpdateRequest.getStatus());
    }

    @Test
    void testAllStatuses() {
        // Test all possible status values
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            // When
            statusUpdateRequest.setStatus(status);

            // Then
            assertEquals(status, statusUpdateRequest.getStatus());
        }
    }

    @Test
    void testNullStatus() {
        // When
        statusUpdateRequest.setStatus(null);

        // Then
        assertNull(statusUpdateRequest.getStatus());
    }
}
