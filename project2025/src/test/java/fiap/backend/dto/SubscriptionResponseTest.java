package fiap.backend.dto;

import fiap.backend.domain.Subscription.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionResponseTest {

    private SubscriptionResponse subscriptionResponse;
    private UUID testId;
    private UUID testUserId;
    private UUID testPlanId;
    private UUID testApprovedByUserId;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testPlanId = UUID.randomUUID();
        testApprovedByUserId = UUID.randomUUID();
        testDateTime = LocalDateTime.now();
        subscriptionResponse = new SubscriptionResponse();
    }

    @Test
    void testDefaultConstructor() {
        // When
        SubscriptionResponse response = new SubscriptionResponse();

        // Then
        assertNull(response.getId());
        assertNull(response.getUserId());
        assertNull(response.getUserEmail());
        assertNull(response.getUserName());
        assertNull(response.getPlanId());
        assertNull(response.getPlanName());
        assertNull(response.getAmount());
        assertNull(response.getStatus());
        assertNull(response.getApprovedByUserId());
        assertNull(response.getApprovedByUserEmail());
        assertNull(response.getSubscriptionDate());
        assertNull(response.getApprovedDate());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        String userEmail = "user@test.com";
        String userName = "John Doe";
        String planName = "Premium Plan";
        BigDecimal amount = BigDecimal.valueOf(29.99);
        SubscriptionStatus status = SubscriptionStatus.APPROVED;
        String approvedByUserEmail = "admin@test.com";

        // When
        subscriptionResponse.setId(testId);
        subscriptionResponse.setUserId(testUserId);
        subscriptionResponse.setUserEmail(userEmail);
        subscriptionResponse.setUserName(userName);
        subscriptionResponse.setPlanId(testPlanId);
        subscriptionResponse.setPlanName(planName);
        subscriptionResponse.setAmount(amount);
        subscriptionResponse.setStatus(status);
        subscriptionResponse.setApprovedByUserId(testApprovedByUserId);
        subscriptionResponse.setApprovedByUserEmail(approvedByUserEmail);
        subscriptionResponse.setSubscriptionDate(testDateTime);
        subscriptionResponse.setApprovedDate(testDateTime);
        subscriptionResponse.setCreatedAt(testDateTime);
        subscriptionResponse.setUpdatedAt(testDateTime);

        // Then
        assertEquals(testId, subscriptionResponse.getId());
        assertEquals(testUserId, subscriptionResponse.getUserId());
        assertEquals(userEmail, subscriptionResponse.getUserEmail());
        assertEquals(userName, subscriptionResponse.getUserName());
        assertEquals(testPlanId, subscriptionResponse.getPlanId());
        assertEquals(planName, subscriptionResponse.getPlanName());
        assertEquals(amount, subscriptionResponse.getAmount());
        assertEquals(status, subscriptionResponse.getStatus());
        assertEquals(testApprovedByUserId, subscriptionResponse.getApprovedByUserId());
        assertEquals(approvedByUserEmail, subscriptionResponse.getApprovedByUserEmail());
        assertEquals(testDateTime, subscriptionResponse.getSubscriptionDate());
        assertEquals(testDateTime, subscriptionResponse.getApprovedDate());
        assertEquals(testDateTime, subscriptionResponse.getCreatedAt());
        assertEquals(testDateTime, subscriptionResponse.getUpdatedAt());
    }

    @Test
    void testAllStatusValues() {
        // Test all possible status values
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            // When
            subscriptionResponse.setStatus(status);

            // Then
            assertEquals(status, subscriptionResponse.getStatus());
        }
    }

    @Test
    void testNullValues() {
        // When - setting all fields to null
        subscriptionResponse.setId(null);
        subscriptionResponse.setUserId(null);
        subscriptionResponse.setUserEmail(null);
        subscriptionResponse.setUserName(null);
        subscriptionResponse.setPlanId(null);
        subscriptionResponse.setPlanName(null);
        subscriptionResponse.setAmount(null);
        subscriptionResponse.setStatus(null);
        subscriptionResponse.setApprovedByUserId(null);
        subscriptionResponse.setApprovedByUserEmail(null);
        subscriptionResponse.setSubscriptionDate(null);
        subscriptionResponse.setApprovedDate(null);
        subscriptionResponse.setCreatedAt(null);
        subscriptionResponse.setUpdatedAt(null);

        // Then
        assertNull(subscriptionResponse.getId());
        assertNull(subscriptionResponse.getUserId());
        assertNull(subscriptionResponse.getUserEmail());
        assertNull(subscriptionResponse.getUserName());
        assertNull(subscriptionResponse.getPlanId());
        assertNull(subscriptionResponse.getPlanName());
        assertNull(subscriptionResponse.getAmount());
        assertNull(subscriptionResponse.getStatus());
        assertNull(subscriptionResponse.getApprovedByUserId());
        assertNull(subscriptionResponse.getApprovedByUserEmail());
        assertNull(subscriptionResponse.getSubscriptionDate());
        assertNull(subscriptionResponse.getApprovedDate());
        assertNull(subscriptionResponse.getCreatedAt());
        assertNull(subscriptionResponse.getUpdatedAt());
    }

    @Test
    void testPendingSubscription() {
        // Given
        subscriptionResponse.setStatus(SubscriptionStatus.PENDING);
        subscriptionResponse.setApprovedByUserId(null);
        subscriptionResponse.setApprovedDate(null);

        // Then
        assertEquals(SubscriptionStatus.PENDING, subscriptionResponse.getStatus());
        assertNull(subscriptionResponse.getApprovedByUserId());
        assertNull(subscriptionResponse.getApprovedDate());
    }

    @Test
    void testApprovedSubscription() {
        // Given
        subscriptionResponse.setStatus(SubscriptionStatus.APPROVED);
        subscriptionResponse.setApprovedByUserId(testApprovedByUserId);
        subscriptionResponse.setApprovedDate(testDateTime);

        // Then
        assertEquals(SubscriptionStatus.APPROVED, subscriptionResponse.getStatus());
        assertEquals(testApprovedByUserId, subscriptionResponse.getApprovedByUserId());
        assertEquals(testDateTime, subscriptionResponse.getApprovedDate());
    }
}
