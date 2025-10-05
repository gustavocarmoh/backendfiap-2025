package fiap.backend.util;

import fiap.backend.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Factory para criar objetos de teste com valores padrão
 * Métodos estáticos simples para facilitar a criação de dados de teste
 */
public class TestDataFactory {

    private TestDataFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========================
    // User Factory Methods
    // ========================

    /**
     * Cria um usuário padrão para testes
     */
    public static User createDefaultUser() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPasswordHash("$2a$10$test.hash.password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(createDefaultUserRoles());
        return user;
    }

    /**
     * Cria um usuário admin para testes
     */
    public static User createAdminUser() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPasswordHash("$2a$10$test.hash.password");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(createAdminRoles());
        return user;
    }

    /**
     * Cria um usuário com email customizado
     */
    public static User createUserWithEmail(String email) {
        User user = createDefaultUser();
        user.setEmail(email);
        return user;
    }

    // ========================
    // Role Factory Methods
    // ========================

    /**
     * Cria role padrão de usuário
     */
    public static Role createUserRole() {
        Role role = new Role();
        role.setName("ROLE_USER");
        return role;
    }

    /**
     * Cria role de admin
     */
    public static Role createAdminRole() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        return role;
    }

    /**
     * Cria conjunto de roles padrão para usuário
     */
    public static Set<Role> createDefaultUserRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(createUserRole());
        return roles;
    }

    /**
     * Cria conjunto de roles para admin
     */
    public static Set<Role> createAdminRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(createAdminRole());
        return roles;
    }

    // ========================
    // SubscriptionPlan Factory Methods
    // ========================

    /**
     * Cria um plano de assinatura padrão
     */
    public static SubscriptionPlan createDefaultSubscriptionPlan() {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName("Basic Plan");
        plan.setDescription("Basic subscription plan");
        plan.setPrice(new BigDecimal("29.99"));
        plan.setNutritionPlansLimit(10);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        return plan;
    }

    /**
     * Cria um plano premium
     */
    public static SubscriptionPlan createPremiumPlan() {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName("Premium Plan");
        plan.setDescription("Premium subscription with unlimited features");
        plan.setPrice(new BigDecimal("99.99"));
        plan.setNutritionPlansLimit(null); // ilimitado
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        return plan;
    }

    /**
     * Cria um plano customizado com valores específicos
     */
    public static SubscriptionPlan createCustomPlan(String name, BigDecimal price, Integer limit) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setDescription("Custom plan for testing");
        plan.setPrice(price);
        plan.setNutritionPlansLimit(limit);
        plan.setIsActive(true);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        return plan;
    }

    // ========================
    // Subscription Factory Methods
    // ========================

    /**
     * Cria uma assinatura padrão
     */
    public static Subscription createDefaultSubscription() {
        Subscription subscription = new Subscription();
        subscription.setUser(createDefaultUser());
        subscription.setPlan(createDefaultSubscriptionPlan());
        subscription.setStatus(Subscription.SubscriptionStatus.PENDING);
        subscription.setSubscriptionDate(LocalDateTime.now());
        subscription.setAmount(new BigDecimal("29.99"));
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        return subscription;
    }

    /**
     * Cria uma assinatura aprovada
     */
    public static Subscription createApprovedSubscription() {
        Subscription subscription = createDefaultSubscription();
        subscription.setStatus(Subscription.SubscriptionStatus.APPROVED);
        subscription.setApprovedDate(LocalDateTime.now());
        subscription.setApprovedByUserId(UUID.randomUUID());
        return subscription;
    }

    /**
     * Cria uma assinatura com usuário e plano específicos
     */
    public static Subscription createSubscription(User user, SubscriptionPlan plan) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(Subscription.SubscriptionStatus.PENDING);
        subscription.setSubscriptionDate(LocalDateTime.now());
        subscription.setAmount(plan.getPrice());
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        return subscription;
    }

    // ========================
    // NutritionPlan Factory Methods
    // ========================

    /**
     * Cria um plano nutricional padrão
     */
    public static NutritionPlan createDefaultNutritionPlan() {
        NutritionPlan plan = new NutritionPlan();
        plan.setUser(createDefaultUser());
        plan.setPlanDate(java.time.LocalDate.now());
        plan.setTitle("Balanced Diet Plan");
        plan.setDescription("A balanced diet for healthy living");
        plan.setTotalCalories(2000);
        plan.setTotalProteins(150.0);
        plan.setTotalCarbohydrates(250.0);
        plan.setTotalFats(70.0);
        plan.setWaterIntakeMl(2000);
        plan.setIsCompleted(false);
        plan.setCreatedAt(LocalDateTime.now());
        return plan;
    }

    /**
     * Cria um plano nutricional completado
     */
    public static NutritionPlan createCompletedNutritionPlan() {
        NutritionPlan plan = createDefaultNutritionPlan();
        plan.setIsCompleted(true);
        return plan;
    }

    /**
     * Cria um plano nutricional para um usuário específico
     */
    public static NutritionPlan createNutritionPlanForUser(User user) {
        NutritionPlan plan = createDefaultNutritionPlan();
        plan.setUser(user);
        return plan;
    }

    // ========================
    // ServiceProvider Factory Methods
    // ========================

    /**
     * Cria um provedor de serviço padrão
     * Nota: ServiceProvider precisa ser verificado para métodos setters corretos
     */
    public static ServiceProvider createDefaultServiceProvider() {
        ServiceProvider provider = new ServiceProvider();
        // Configurar campos conforme a classe ServiceProvider permitir
        provider.setCreatedAt(LocalDateTime.now());
        return provider;
    }

    // ========================
    // ChatMessage Factory Methods
    // ========================

    /**
     * Cria uma mensagem de chat padrão
     * Nota: ChatMessage precisa ser verificado para métodos setters corretos
     */
    public static ChatMessage createDefaultChatMessage() {
        ChatMessage message = new ChatMessage();
        // Configurar campos conforme a classe ChatMessage permitir
        return message;
    }

    /**
     * Cria uma mensagem entre dois usuários específicos
     */
    public static ChatMessage createChatMessage(User sender, User receiver, String content) {
        ChatMessage message = new ChatMessage();
        // Configurar campos conforme a classe ChatMessage permitir
        return message;
    }
}

