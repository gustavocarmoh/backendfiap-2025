package fiap.backend.service;

import fiap.backend.domain.NutritionPlan;
import fiap.backend.domain.Subscription;
import fiap.backend.domain.User;
import fiap.backend.dto.*;
import fiap.backend.repository.NutritionPlanRepository;
import fiap.backend.repository.SubscriptionRepository;
import fiap.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de planos nutricionais
 */
@Service
@Transactional
public class NutritionPlanService {

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Cria um novo plano nutricional para o usuário
     */
    public NutritionPlanResponseDTO createNutritionPlan(String userEmail, CreateNutritionPlanDTO createDTO) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica se o usuário pode criar mais planos nutricionais
        validateNutritionPlanLimits(user);

        // Verifica se já existe um plano para a data especificada
        if (nutritionPlanRepository.existsByUserAndPlanDate(user, createDTO.getPlanDate())) {
            throw new RuntimeException("Já existe um plano nutricional para esta data");
        }

        NutritionPlan nutritionPlan = new NutritionPlan(user, createDTO.getPlanDate(), createDTO.getTitle());
        updateNutritionPlanFromDTO(nutritionPlan, createDTO);

        nutritionPlan = nutritionPlanRepository.save(nutritionPlan);

        return convertToResponseDTO(nutritionPlan);
    }

    /**
     * Busca plano nutricional por ID
     */
    @Transactional(readOnly = true)
    public NutritionPlanResponseDTO getNutritionPlanById(String userEmail, UUID planId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        NutritionPlan nutritionPlan = nutritionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plano nutricional não encontrado"));

        // Verifica se o plano pertence ao usuário
        if (!nutritionPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado ao plano nutricional");
        }

        return convertToResponseDTO(nutritionPlan);
    }

    /**
     * Busca plano nutricional de hoje
     */
    @Transactional(readOnly = true)
    public Optional<NutritionPlanResponseDTO> getTodayNutritionPlan(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        LocalDate today = LocalDate.now();
        Optional<NutritionPlan> plan = nutritionPlanRepository.findTodayPlan(user, today);

        return plan.map(this::convertToResponseDTO);
    }

    /**
     * Busca planos nutricionais da semana atual
     */
    @Transactional(readOnly = true)
    public WeeklyNutritionPlanResponseDTO getWeeklyNutritionPlans(String userEmail) {
        return getWeeklyNutritionPlans(userEmail, LocalDate.now());
    }

    /**
     * Busca planos nutricionais de uma semana específica
     */
    @Transactional(readOnly = true)
    public WeeklyNutritionPlanResponseDTO getWeeklyNutritionPlans(String userEmail, LocalDate referenceDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Calcula início e fim da semana (segunda a domingo)
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<NutritionPlan> plans = nutritionPlanRepository.findWeekPlans(user, weekStart, weekEnd);
        List<NutritionPlanResponseDTO> planDTOs = plans.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        WeeklyNutritionPlanResponseDTO weeklyResponse = new WeeklyNutritionPlanResponseDTO(weekStart, weekEnd,
                planDTOs);

        // Calcula estatísticas da semana
        WeeklyNutritionPlanResponseDTO.WeeklyStatsDTO stats = calculateWeeklyStats(plans);
        weeklyResponse.setWeeklyStats(stats);

        return weeklyResponse;
    }

    /**
     * Lista planos nutricionais do usuário com paginação
     */
    @Transactional(readOnly = true)
    public Page<NutritionPlanResponseDTO> getUserNutritionPlans(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "planDate"));
        Page<NutritionPlan> plans = nutritionPlanRepository.findByUserOrderByPlanDateDesc(user, pageable);

        return plans.map(this::convertToResponseDTO);
    }

    /**
     * Atualiza um plano nutricional
     */
    public NutritionPlanResponseDTO updateNutritionPlan(String userEmail, UUID planId,
            UpdateNutritionPlanDTO updateDTO) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        NutritionPlan nutritionPlan = nutritionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plano nutricional não encontrado"));

        // Verifica se o plano pertence ao usuário
        if (!nutritionPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado ao plano nutricional");
        }

        updateNutritionPlanFromDTO(nutritionPlan, updateDTO);
        nutritionPlan = nutritionPlanRepository.save(nutritionPlan);

        return convertToResponseDTO(nutritionPlan);
    }

    /**
     * Marca um plano como completo/incompleto
     */
    public NutritionPlanResponseDTO togglePlanCompletion(String userEmail, UUID planId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        NutritionPlan nutritionPlan = nutritionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plano nutricional não encontrado"));

        // Verifica se o plano pertence ao usuário
        if (!nutritionPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado ao plano nutricional");
        }

        // Alterna o status de conclusão
        if (nutritionPlan.getIsCompleted()) {
            nutritionPlan.markAsIncomplete();
        } else {
            nutritionPlan.markAsCompleted();
        }

        nutritionPlan = nutritionPlanRepository.save(nutritionPlan);
        return convertToResponseDTO(nutritionPlan);
    }

    /**
     * Remove um plano nutricional
     */
    public void deleteNutritionPlan(String userEmail, UUID planId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        NutritionPlan nutritionPlan = nutritionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plano nutricional não encontrado"));

        // Verifica se o plano pertence ao usuário
        if (!nutritionPlan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado ao plano nutricional");
        }

        nutritionPlanRepository.delete(nutritionPlan);
    }

    /**
     * Busca planos pendentes do usuário
     */
    @Transactional(readOnly = true)
    public List<NutritionPlanResponseDTO> getPendingPlans(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<NutritionPlan> pendingPlans = nutritionPlanRepository.findPendingPlans(user, LocalDate.now());

        return pendingPlans.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca planos futuros do usuário
     */
    @Transactional(readOnly = true)
    public List<NutritionPlanResponseDTO> getFuturePlans(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<NutritionPlan> futurePlans = nutritionPlanRepository.findFuturePlans(user, LocalDate.now());

        return futurePlans.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se o usuário pode criar mais planos nutricionais baseado no seu
     * plano de assinatura
     */
    private void validateNutritionPlanLimits(User user) {
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptionsByUserId(user.getId());

        if (activeSubscriptions.isEmpty()) {
            throw new RuntimeException("Usuário não possui assinatura ativa");
        }

        // Pega a primeira assinatura ativa (assumindo que só existe uma por vez)
        Subscription subscription = activeSubscriptions.get(0);
        Integer nutritionPlanLimit = subscription.getPlan().getNutritionPlansLimit();

        // Se o limite é null, significa que é ilimitado
        if (nutritionPlanLimit == null) {
            return;
        }

        // Conta quantos planos o usuário já possui
        long currentPlanCount = nutritionPlanRepository.countByUser(user);

        if (currentPlanCount >= nutritionPlanLimit) {
            throw new RuntimeException(String.format(
                    "Limite de planos nutricionais atingido. Seu plano permite %d planos e você já possui %d. " +
                            "Considere fazer upgrade do seu plano de assinatura.",
                    nutritionPlanLimit, currentPlanCount));
        }
    }

    /**
     * Atualiza entidade NutritionPlan a partir do DTO de criação
     */
    private void updateNutritionPlanFromDTO(NutritionPlan nutritionPlan, CreateNutritionPlanDTO dto) {
        if (dto.getDescription() != null)
            nutritionPlan.setDescription(dto.getDescription());
        if (dto.getBreakfast() != null)
            nutritionPlan.setBreakfast(dto.getBreakfast());
        if (dto.getMorningSnack() != null)
            nutritionPlan.setMorningSnack(dto.getMorningSnack());
        if (dto.getLunch() != null)
            nutritionPlan.setLunch(dto.getLunch());
        if (dto.getAfternoonSnack() != null)
            nutritionPlan.setAfternoonSnack(dto.getAfternoonSnack());
        if (dto.getDinner() != null)
            nutritionPlan.setDinner(dto.getDinner());
        if (dto.getEveningSnack() != null)
            nutritionPlan.setEveningSnack(dto.getEveningSnack());
        if (dto.getTotalCalories() != null)
            nutritionPlan.setTotalCalories(dto.getTotalCalories());
        if (dto.getTotalProteins() != null)
            nutritionPlan.setTotalProteins(dto.getTotalProteins());
        if (dto.getTotalCarbohydrates() != null)
            nutritionPlan.setTotalCarbohydrates(dto.getTotalCarbohydrates());
        if (dto.getTotalFats() != null)
            nutritionPlan.setTotalFats(dto.getTotalFats());
        if (dto.getWaterIntakeMl() != null)
            nutritionPlan.setWaterIntakeMl(dto.getWaterIntakeMl());
        if (dto.getNotes() != null)
            nutritionPlan.setNotes(dto.getNotes());
    }

    /**
     * Atualiza entidade NutritionPlan a partir do DTO de atualização
     */
    private void updateNutritionPlanFromDTO(NutritionPlan nutritionPlan, UpdateNutritionPlanDTO dto) {
        if (dto.getTitle() != null)
            nutritionPlan.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            nutritionPlan.setDescription(dto.getDescription());
        if (dto.getBreakfast() != null)
            nutritionPlan.setBreakfast(dto.getBreakfast());
        if (dto.getMorningSnack() != null)
            nutritionPlan.setMorningSnack(dto.getMorningSnack());
        if (dto.getLunch() != null)
            nutritionPlan.setLunch(dto.getLunch());
        if (dto.getAfternoonSnack() != null)
            nutritionPlan.setAfternoonSnack(dto.getAfternoonSnack());
        if (dto.getDinner() != null)
            nutritionPlan.setDinner(dto.getDinner());
        if (dto.getEveningSnack() != null)
            nutritionPlan.setEveningSnack(dto.getEveningSnack());
        if (dto.getTotalCalories() != null)
            nutritionPlan.setTotalCalories(dto.getTotalCalories());
        if (dto.getTotalProteins() != null)
            nutritionPlan.setTotalProteins(dto.getTotalProteins());
        if (dto.getTotalCarbohydrates() != null)
            nutritionPlan.setTotalCarbohydrates(dto.getTotalCarbohydrates());
        if (dto.getTotalFats() != null)
            nutritionPlan.setTotalFats(dto.getTotalFats());
        if (dto.getWaterIntakeMl() != null)
            nutritionPlan.setWaterIntakeMl(dto.getWaterIntakeMl());
        if (dto.getNotes() != null)
            nutritionPlan.setNotes(dto.getNotes());
    }

    /**
     * Converte entidade NutritionPlan para DTO de resposta
     */
    private NutritionPlanResponseDTO convertToResponseDTO(NutritionPlan nutritionPlan) {
        NutritionPlanResponseDTO dto = new NutritionPlanResponseDTO();
        dto.setId(nutritionPlan.getId());
        dto.setPlanDate(nutritionPlan.getPlanDate());
        dto.setTitle(nutritionPlan.getTitle());
        dto.setDescription(nutritionPlan.getDescription());
        dto.setBreakfast(nutritionPlan.getBreakfast());
        dto.setMorningSnack(nutritionPlan.getMorningSnack());
        dto.setLunch(nutritionPlan.getLunch());
        dto.setAfternoonSnack(nutritionPlan.getAfternoonSnack());
        dto.setDinner(nutritionPlan.getDinner());
        dto.setEveningSnack(nutritionPlan.getEveningSnack());
        dto.setTotalCalories(nutritionPlan.getTotalCalories());
        dto.setTotalProteins(nutritionPlan.getTotalProteins());
        dto.setTotalCarbohydrates(nutritionPlan.getTotalCarbohydrates());
        dto.setTotalFats(nutritionPlan.getTotalFats());
        dto.setWaterIntakeMl(nutritionPlan.getWaterIntakeMl());
        dto.setIsCompleted(nutritionPlan.getIsCompleted());
        dto.setCompletedAt(nutritionPlan.getCompletedAt());
        dto.setNotes(nutritionPlan.getNotes());
        dto.setCreatedAt(nutritionPlan.getCreatedAt());
        dto.setUpdatedAt(nutritionPlan.getUpdatedAt());

        // Flags de status
        dto.setIsToday(nutritionPlan.isToday());
        dto.setIsOverdue(nutritionPlan.isOverdue());

        return dto;
    }

    /**
     * Calcula estatísticas semanais
     */
    private WeeklyNutritionPlanResponseDTO.WeeklyStatsDTO calculateWeeklyStats(List<NutritionPlan> plans) {
        WeeklyNutritionPlanResponseDTO.WeeklyStatsDTO stats = new WeeklyNutritionPlanResponseDTO.WeeklyStatsDTO();

        stats.setTotalPlans(plans.size());

        long completedCount = plans.stream().mapToLong(p -> p.getIsCompleted() ? 1 : 0).sum();
        stats.setCompletedPlans((int) completedCount);
        stats.setPendingPlans(plans.size() - (int) completedCount);

        // Taxa de conclusão
        if (plans.size() > 0) {
            stats.setCompletionRate((double) completedCount / plans.size() * 100);
        } else {
            stats.setCompletionRate(0.0);
        }

        // Média de calorias
        OptionalDouble avgCalories = plans.stream()
                .filter(p -> p.getTotalCalories() != null)
                .mapToInt(NutritionPlan::getTotalCalories)
                .average();
        stats.setAverageCalories(avgCalories.orElse(0.0));

        // Total de água consumida
        double totalWater = plans.stream()
                .filter(p -> p.getWaterIntakeMl() != null)
                .mapToInt(NutritionPlan::getWaterIntakeMl)
                .sum() / 1000.0; // Converte para litros
        stats.setTotalWaterIntake(totalWater);

        return stats;
    }
}
