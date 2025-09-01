package fiap.backend.controller;

import fiap.backend.domain.UserPhoto;
import fiap.backend.dto.UserPhotoResponse;
import fiap.backend.service.CurrentUserService;
import fiap.backend.service.UserPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller responsável pelo gerenciamento de fotos de perfil dos usuários.
 * Fornece endpoints para upload, visualização e remoção de fotos.
 */
@RestController
@RequestMapping("/api/v1/user/photo")
@Tag(name = "User Photos", description = "Gerenciamento de fotos de perfil dos usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserPhotoController {

    private final UserPhotoService userPhotoService;
    private final CurrentUserService currentUserService;

    public UserPhotoController(UserPhotoService userPhotoService, CurrentUserService currentUserService) {
        this.userPhotoService = userPhotoService;
        this.currentUserService = currentUserService;
    }

    /**
     * Faz upload de uma nova foto de perfil para o usuário logado
     * 
     * @param file arquivo de imagem a ser enviado
     * @return resposta com status do upload e URL da foto
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Upload de foto de perfil", description = "Permite ao usuário fazer upload de sua foto de perfil. Aceita JPEG, PNG e GIF até 5MB.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto enviada com sucesso", content = @Content(schema = @Schema(implementation = UserPhotoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou muito grande"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<UserPhotoResponse> uploadPhoto(
            @Parameter(description = "Arquivo de imagem (JPEG, PNG, GIF)", required = true) @RequestParam("file") MultipartFile file) {

        try {
            UUID currentUserId = currentUserService.getCurrentUserId();
            UserPhotoResponse response = userPhotoService.uploadUserPhoto(currentUserId, file);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserPhotoResponse.error("Erro ao processar arquivo: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserPhotoResponse.error("Erro interno: " + e.getMessage()));
        }
    }

    /**
     * Recupera a foto de perfil do usuário logado
     * 
     * @return foto como array de bytes com headers apropriados
     */
    @GetMapping("/my-photo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Obter minha foto de perfil", description = "Retorna a foto de perfil do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto encontrada"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<byte[]> getMyPhoto() {
        try {
            UUID currentUserId = currentUserService.getCurrentUserId();
            Optional<UserPhoto> userPhoto = userPhotoService.getUserPhoto(currentUserId);

            if (userPhoto.isPresent()) {
                UserPhoto photo = userPhoto.get();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(photo.getContentType()));
                headers.setContentLength(photo.getPhotoData().length);
                headers.setCacheControl("max-age=3600"); // Cache por 1 hora

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(photo.getPhotoData());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recupera uma foto específica pelo ID (público)
     * 
     * @param photoId ID da foto
     * @return foto como array de bytes
     */
    @GetMapping("/{photoId}")
    @Operation(summary = "Obter foto por ID", description = "Retorna uma foto específica pelo seu ID (endpoint público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto encontrada"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<byte[]> getPhotoById(
            @Parameter(description = "ID da foto", required = true) @PathVariable UUID photoId) {

        try {
            Optional<UserPhoto> userPhoto = userPhotoService.getPhotoById(photoId);

            if (userPhoto.isPresent()) {
                UserPhoto photo = userPhoto.get();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(photo.getContentType()));
                headers.setContentLength(photo.getPhotoData().length);
                headers.setCacheControl("max-age=3600"); // Cache por 1 hora

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(photo.getPhotoData());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Remove a foto de perfil do usuário logado
     * 
     * @return resposta confirmando a remoção
     */
    @DeleteMapping("/my-photo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Remover minha foto de perfil", description = "Remove a foto de perfil do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Foto não encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<UserPhotoResponse> deleteMyPhoto() {
        try {
            UUID currentUserId = currentUserService.getCurrentUserId();
            UserPhotoResponse response = userPhotoService.deleteUserPhoto(currentUserId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserPhotoResponse.error("Erro interno: " + e.getMessage()));
        }
    }

    /**
     * Verifica se o usuário logado possui foto de perfil
     * 
     * @return informações sobre a existência da foto
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Status da foto de perfil", description = "Verifica se o usuário possui foto de perfil e retorna metadados")
    public ResponseEntity<Map<String, Object>> getPhotoStatus() {
        try {
            UUID currentUserId = currentUserService.getCurrentUserId();

            Map<String, Object> response = new HashMap<>();
            response.put("hasPhoto", userPhotoService.userHasPhoto(currentUserId));

            Optional<UserPhoto> metadata = userPhotoService.getPhotoMetadata(currentUserId);
            if (metadata.isPresent()) {
                UserPhoto photo = metadata.get();
                response.put("fileName", photo.getFileName());
                response.put("contentType", photo.getContentType());
                response.put("fileSize", photo.getFileSize());
                response.put("uploadedAt", photo.getCreatedAt());
                response.put("updatedAt", photo.getUpdatedAt());
                response.put("photoUrl", "/api/v1/user/photo/" + photo.getId());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint administrativo para obter estatísticas de armazenamento
     * 
     * @return estatísticas de uso de storage
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estatísticas de fotos (Admin)", description = "Retorna estatísticas de uso de armazenamento de fotos (apenas administradores)")
    public ResponseEntity<Map<String, Object>> getStorageStats() {
        try {
            UserPhotoService.PhotoStorageStats stats = userPhotoService.getStorageStats();

            Map<String, Object> response = new HashMap<>();
            response.put("totalPhotos", stats.getTotalPhotos());
            response.put("totalStorageBytes", stats.getTotalStorageBytes());
            response.put("totalStorageMB", Math.round(stats.getTotalStorageMB() * 100.0) / 100.0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
