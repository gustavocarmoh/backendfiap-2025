package fiap.backend.service;

import fiap.backend.domain.User;
import fiap.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Serviço para acessar informações do usuário atualmente logado.
 * Obtém dados do contexto de segurança e atributos da requisição (token JWT).
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtém o ID do usuário atualmente logado
     * 
     * @return UUID do usuário
     * @throws RuntimeException se o usuário não for encontrado
     */
    public UUID getCurrentUserId() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return user.getId();
    }

    /**
     * Obtém a entidade completa do usuário atualmente logado
     * 
     * @return entidade User
     * @throws RuntimeException se o usuário não for encontrado
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Obtém o email do usuário atualmente logado do contexto de segurança
     * 
     * @return email do usuário
     * @throws RuntimeException se o usuário não estiver autenticado
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getName();
    }

    /**
     * Obtém o nome do plano ativo do usuário logado (do token JWT)
     * 
     * @return Nome do plano ativo ou null se não tiver plano ativo
     */
    public String getCurrentUserActivePlanName() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return (String) request.getAttribute("activePlanName");
        }
        return null;
    }

    /**
     * Obtém o ID do plano ativo do usuário logado (do token JWT)
     * 
     * @return ID do plano ativo ou null se não tiver plano ativo
     */
    public String getCurrentUserActivePlanId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return (String) request.getAttribute("activePlanId");
        }
        return null;
    }

    /**
     * Verifica se o usuário logado tem um plano ativo
     */
    public boolean hasActivePlan() {
        return getCurrentUserActivePlanName() != null;
    }

    /**
     * Obtém informações completas do plano ativo do usuário logado
     */
    public UserPlanInfo getCurrentUserPlanInfo() {
        return new UserPlanInfo(
                getCurrentUserEmail(),
                getCurrentUserId(),
                getCurrentUserActivePlanName(),
                getCurrentUserActivePlanId());
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Classe para encapsular informações do usuário e plano
     */
    public static class UserPlanInfo {
        private final String email;
        private final UUID userId;
        private final String activePlanName;
        private final String activePlanId;

        public UserPlanInfo(String email, UUID userId, String activePlanName, String activePlanId) {
            this.email = email;
            this.userId = userId;
            this.activePlanName = activePlanName;
            this.activePlanId = activePlanId;
        }

        public String getEmail() {
            return email;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getActivePlanName() {
            return activePlanName;
        }

        public String getActivePlanId() {
            return activePlanId;
        }

        public boolean hasActivePlan() {
            return activePlanName != null;
        }

        @Override
        public String toString() {
            return String.format("UserPlanInfo{email='%s', userId=%s, activePlan='%s'}",
                    email, userId, activePlanName != null ? activePlanName : "None");
        }
    }
}
