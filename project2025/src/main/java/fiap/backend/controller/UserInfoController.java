package fiap.backend.controller;

import fiap.backend.service.CurrentUserService;
import fiap.backend.service.UserPhotoService;
import fiap.backend.domain.UserPhoto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller para fornecer informações completas do usuário logado.
 * Inclui dados pessoais, plano ativo e informações da foto de perfil.
 */
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Info", description = "Endpoints para informações do usuário logado")
@SecurityRequirement(name = "bearerAuth")
public class UserInfoController {

    private final CurrentUserService currentUserService;
    private final UserPhotoService userPhotoService;

    public UserInfoController(CurrentUserService currentUserService, UserPhotoService userPhotoService) {
        this.currentUserService = currentUserService;
        this.userPhotoService = userPhotoService;
    }

    /**
     * Obtém informações completas do usuário logado
     * 
     * @return dados do usuário incluindo plano ativo e foto de perfil
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Obter informações do usuário logado", description = "Retorna informações completas do usuário autenticado, incluindo plano ativo e foto de perfil")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo() {
        CurrentUserService.UserPlanInfo userPlanInfo = currentUserService.getCurrentUserPlanInfo();
        UUID currentUserId = currentUserService.getCurrentUserId();

        Map<String, Object> response = new HashMap<>();

        // Informações básicas do usuário
        response.put("email", userPlanInfo.getEmail());
        response.put("userId", userPlanInfo.getUserId());

        // Informações do plano ativo
        response.put("hasActivePlan", userPlanInfo.hasActivePlan());
        response.put("activePlanName", userPlanInfo.getActivePlanName());
        response.put("activePlanId", userPlanInfo.getActivePlanId());

        // Informações da foto de perfil
        addPhotoInfoToResponse(response, currentUserId);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtém apenas informações do plano ativo do usuário logado
     * 
     * @return dados do plano ativo
     */
    @GetMapping("/plan")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Obter plano ativo do usuário", description = "Retorna apenas informações do plano ativo do usuário logado")
    public ResponseEntity<Map<String, Object>> getCurrentUserPlan() {
        Map<String, Object> response = new HashMap<>();
        response.put("hasActivePlan", currentUserService.hasActivePlan());
        response.put("activePlanName", currentUserService.getCurrentUserActivePlanName());
        response.put("activePlanId", currentUserService.getCurrentUserActivePlanId());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtém informações completas do perfil do usuário
     * 
     * @return dados completos do perfil incluindo foto
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Obter perfil completo", description = "Retorna informações completas do perfil do usuário incluindo dados pessoais e foto")
    public ResponseEntity<Map<String, Object>> getCompleteProfile() {
        CurrentUserService.UserPlanInfo userPlanInfo = currentUserService.getCurrentUserPlanInfo();
        UUID currentUserId = currentUserService.getCurrentUserId();

        Map<String, Object> response = new HashMap<>();

        // Informações do usuário
        response.put("userId", userPlanInfo.getUserId());
        response.put("email", userPlanInfo.getEmail());

        // Informações do plano
        Map<String, Object> planInfo = new HashMap<>();
        planInfo.put("hasActivePlan", userPlanInfo.hasActivePlan());
        planInfo.put("activePlanName", userPlanInfo.getActivePlanName());
        planInfo.put("activePlanId", userPlanInfo.getActivePlanId());
        response.put("plan", planInfo);

        // Informações da foto de perfil
        Map<String, Object> photoInfo = new HashMap<>();
        addDetailedPhotoInfoToMap(photoInfo, currentUserId);
        response.put("photo", photoInfo);

        return ResponseEntity.ok(response);
    }

    /**
     * Adiciona informações básicas da foto à resposta
     * 
     * @param response mapa de resposta
     * @param userId   ID do usuário
     */
    private void addPhotoInfoToResponse(Map<String, Object> response, UUID userId) {
        try {
            boolean hasPhoto = userPhotoService.userHasPhoto(userId);
            response.put("hasProfilePhoto", hasPhoto);

            // Sempre retorna uma URL - seja da foto personalizada ou da padrão
            String photoUrl = userPhotoService.getUserPhotoUrl(userId);
            response.put("profilePhotoUrl", photoUrl);

        } catch (Exception e) {
            response.put("hasProfilePhoto", false);
            // Em caso de erro, usar foto padrão
            response.put("profilePhotoUrl", userPhotoService.getDefaultPhotoUrl());
        }
    }

    /**
     * Adiciona informações detalhadas da foto ao mapa
     * 
     * @param photoMap mapa para informações da foto
     * @param userId   ID do usuário
     */
    private void addDetailedPhotoInfoToMap(Map<String, Object> photoMap, UUID userId) {
        try {
            boolean hasPhoto = userPhotoService.userHasPhoto(userId);
            photoMap.put("hasPhoto", hasPhoto);

            if (hasPhoto) {
                Optional<UserPhoto> photoMetadata = userPhotoService.getPhotoMetadata(userId);
                if (photoMetadata.isPresent()) {
                    UserPhoto photo = photoMetadata.get();
                    photoMap.put("fileName", photo.getFileName());
                    photoMap.put("contentType", photo.getContentType());
                    photoMap.put("fileSize", photo.getFileSize());
                    photoMap.put("uploadedAt", photo.getCreatedAt());
                    photoMap.put("updatedAt", photo.getUpdatedAt());
                    photoMap.put("photoUrl", "/api/v1/user/photo/my-photo");
                    photoMap.put("directUrl", "/api/v1/user/photo/" + photo.getId());
                } else {
                    photoMap.put("photoUrl", userPhotoService.getDefaultPhotoUrl());
                }
            } else {
                // Usuário sem foto personalizada - usar foto padrão
                photoMap.put("photoUrl", userPhotoService.getDefaultPhotoUrl());
                photoMap.put("isDefault", true);
            }
        } catch (Exception e) {
            photoMap.put("hasPhoto", false);
            photoMap.put("photoUrl", userPhotoService.getDefaultPhotoUrl());
            photoMap.put("error", "Erro ao carregar informações da foto");
        }
    }
}
