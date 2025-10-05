package fiap.backend.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Constantes reutilizáveis para testes
 * Facilita a manutenção e padronização dos dados de teste
 */
public final class TestConstants {

    private TestConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // User Constants
    public static final String USER_ID = "user-123";
    public static final String USER_EMAIL = "user@test.com";
    public static final String USER_PASSWORD = "Password123!";
    public static final String USER_FIRST_NAME = "John";
    public static final String USER_LAST_NAME = "Doe";
    public static final String USER_FULL_NAME = USER_FIRST_NAME + " " + USER_LAST_NAME;

    // Admin Constants
    public static final String ADMIN_ID = "admin-123";
    public static final String ADMIN_EMAIL = "admin@test.com";
    public static final String ADMIN_PASSWORD = "AdminPass123!";
    public static final String ADMIN_FIRST_NAME = "Admin";
    public static final String ADMIN_LAST_NAME = "User";

    // JWT Constants
    public static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    public static final String JWT_SECRET = "test-secret-key-for-jwt-token-generation-min-256-bits";
    public static final long JWT_EXPIRATION_MS = 86400000L; // 24 hours

    // Subscription Plan Constants
    public static final String PLAN_ID = "plan-123";
    public static final String PLAN_NAME = "Premium Plan";
    public static final String PLAN_DESCRIPTION = "Premium subscription with all features";
    public static final BigDecimal PLAN_PRICE = new BigDecimal("99.99");
    public static final Integer PLAN_DURATION_DAYS = 30;

    // Subscription Constants
    public static final String SUBSCRIPTION_ID = "subscription-123";
    public static final LocalDate SUBSCRIPTION_START_DATE = LocalDate.now();
    public static final LocalDate SUBSCRIPTION_END_DATE = LocalDate.now().plusDays(30);
    public static final String SUBSCRIPTION_STATUS_PENDING = "PENDING";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "ACTIVE";
    public static final String SUBSCRIPTION_STATUS_CANCELLED = "CANCELLED";
    public static final String SUBSCRIPTION_STATUS_EXPIRED = "EXPIRED";

    // Nutrition Plan Constants
    public static final String NUTRITION_PLAN_ID = "nutrition-plan-123";
    public static final String NUTRITION_PLAN_TITLE = "Balanced Diet Plan";
    public static final String NUTRITION_PLAN_DESCRIPTION = "A balanced diet for healthy living";
    public static final LocalDate NUTRITION_PLAN_DATE = LocalDate.now();
    public static final BigDecimal NUTRITION_TOTAL_CALORIES = new BigDecimal("2000");
    public static final BigDecimal NUTRITION_TOTAL_PROTEINS = new BigDecimal("150");
    public static final BigDecimal NUTRITION_TOTAL_CARBS = new BigDecimal("250");
    public static final BigDecimal NUTRITION_TOTAL_FATS = new BigDecimal("70");
    public static final Integer NUTRITION_WATER_INTAKE = 2000;

    // Chat Constants
    public static final String CHAT_MESSAGE_ID = "message-123";
    public static final String CHAT_MESSAGE_CONTENT = "Hello, how can I help you?";
    public static final LocalDateTime CHAT_MESSAGE_TIMESTAMP = LocalDateTime.now();

    // Service Provider Constants
    public static final String PROVIDER_ID = "provider-123";
    public static final String PROVIDER_NAME = "Dr. Smith";
    public static final String PROVIDER_SPECIALTY = "Nutritionist";
    public static final String PROVIDER_EMAIL = "provider@test.com";
    public static final String PROVIDER_PHONE = "+55 11 99999-9999";

    // Photo Constants
    public static final String PHOTO_ID = "photo-123";
    public static final String PHOTO_URL = "https://example.com/photo.jpg";
    public static final String PHOTO_DESCRIPTION = "Profile photo";

    // Validation Messages
    public static final String VALIDATION_EMAIL_INVALID = "Invalid email format";
    public static final String VALIDATION_FIELD_REQUIRED = "Field is required";
    public static final String VALIDATION_PASSWORD_WEAK = "Password is too weak";

    // Error Messages
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_PLAN_NOT_FOUND = "Plan not found";
    public static final String ERROR_SUBSCRIPTION_NOT_FOUND = "Subscription not found";
    public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
    public static final String ERROR_FORBIDDEN = "Forbidden access";

    // HTTP Status Messages
    public static final String SUCCESS_MESSAGE = "Operation successful";
    public static final String CREATED_MESSAGE = "Resource created successfully";
    public static final String UPDATED_MESSAGE = "Resource updated successfully";
    public static final String DELETED_MESSAGE = "Resource deleted successfully";
}
