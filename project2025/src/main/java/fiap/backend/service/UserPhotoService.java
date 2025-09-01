package fiap.backend.service;

import fiap.backend.domain.User;
import fiap.backend.domain.UserPhoto;
import fiap.backend.dto.UserPhotoResponse;
import fiap.backend.repository.UserPhotoRepository;
import fiap.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pelo gerenciamento de fotos de perfil dos usuários.
 * Handles upload, recuperação, atualização e remoção de fotos de usuário.
 */
@Service
@Transactional
public class UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;
    private final UserRepository userRepository;

    // Tipos de arquivo permitidos para upload
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif");

    // Tamanho máximo permitido para arquivos (5MB)
    @Value("${app.upload.max-file-size:5242880}")
    private long maxFileSize;

    // URL da foto padrão para usuários sem foto
    @Value("${app.default-photo.url:/api/photos/default}")
    private String defaultPhotoUrl;

    public UserPhotoService(UserPhotoRepository userPhotoRepository, UserRepository userRepository) {
        this.userPhotoRepository = userPhotoRepository;
        this.userRepository = userRepository;
    }

    /**
     * Faz upload de uma nova foto de perfil para o usuário.
     * Remove automaticamente a foto anterior se existir para manter apenas uma foto
     * por usuário.
     * 
     * @param userId ID do usuário
     * @param file   arquivo de imagem
     * @return resposta com status e URL da foto
     * @throws IOException se houver erro na leitura do arquivo
     */
    public UserPhotoResponse uploadUserPhoto(UUID userId, MultipartFile file) throws IOException {
        // Validar se o usuário existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Validar arquivo
        String validationError = validateFile(file);
        if (validationError != null) {
            return UserPhotoResponse.error(validationError);
        }

        try {
            // Converter arquivo para bytes
            byte[] photoData = file.getBytes();

            // Verificar se o usuário já possui foto e remover a antiga
            Optional<UserPhoto> existingPhoto = userPhotoRepository.findByUser(user);
            if (existingPhoto.isPresent()) {
                // Remover foto antiga para não onerar o banco
                userPhotoRepository.delete(existingPhoto.get());
                userPhotoRepository.flush(); // Garantir que a exclusão seja processada
            }

            // Criar nova foto (sempre uma nova entrada para evitar problemas de cache)
            UserPhoto userPhoto = new UserPhoto(user, photoData, file.getOriginalFilename(), file.getContentType());

            // Salvar no banco
            userPhoto = userPhotoRepository.save(userPhoto);

            // Atualizar URL da foto no usuário
            String photoUrl = generatePhotoUrl(userPhoto.getId());
            user.setProfilePhotoUrl(photoUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return new UserPhotoResponse(
                    photoUrl,
                    "Foto de perfil atualizada com sucesso",
                    true,
                    userPhoto.getFileSize(),
                    userPhoto.getContentType());

        } catch (Exception e) {
            return UserPhotoResponse.error("Erro interno do servidor: " + e.getMessage());
        }
    }

    /**
     * Recupera a foto de perfil de um usuário
     * 
     * @param userId ID do usuário
     * @return Optional contendo a foto se encontrada
     */
    public Optional<UserPhoto> getUserPhoto(UUID userId) {
        return userPhotoRepository.findByUserId(userId);
    }

    /**
     * Recupera a URL da foto de perfil do usuário ou retorna a foto padrão
     * 
     * @param userId ID do usuário
     * @return URL da foto do usuário ou URL da foto padrão
     */
    public String getUserPhotoUrl(UUID userId) {
        Optional<UserPhoto> userPhoto = userPhotoRepository.findByUserId(userId);
        if (userPhoto.isPresent()) {
            return generatePhotoUrl(userPhoto.get().getId());
        } else {
            return defaultPhotoUrl;
        }
    }

    /**
     * Obtém a foto padrão para usuários sem foto personalizada
     * 
     * @return URL da foto padrão
     */
    public String getDefaultPhotoUrl() {
        return defaultPhotoUrl;
    }

    /**
     * Recupera a foto de perfil pelo ID da foto
     * 
     * @param photoId ID da foto
     * @return Optional contendo a foto se encontrada
     */
    public Optional<UserPhoto> getPhotoById(UUID photoId) {
        return userPhotoRepository.findById(photoId);
    }

    /**
     * Verifica se um usuário possui foto de perfil
     * 
     * @param userId ID do usuário
     * @return true se o usuário possui foto
     */
    public boolean userHasPhoto(UUID userId) {
        return userPhotoRepository.existsByUserId(userId);
    }

    /**
     * Remove a foto de perfil de um usuário e define a foto padrão
     * 
     * @param userId ID do usuário
     * @return resposta indicando sucesso ou erro
     */
    public UserPhotoResponse deleteUserPhoto(UUID userId) {
        try {
            // Verificar se o usuário existe
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Verificar se possui foto
            if (!userPhotoRepository.existsByUserId(userId)) {
                return UserPhotoResponse.error("Usuário não possui foto de perfil");
            }

            // Remover foto do banco
            userPhotoRepository.deleteByUserId(userId);

            // Definir foto padrão no usuário
            user.setProfilePhotoUrl(defaultPhotoUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return UserPhotoResponse.success(defaultPhotoUrl, "Foto de perfil removida, foto padrão definida");

        } catch (Exception e) {
            return UserPhotoResponse.error("Erro ao remover foto: " + e.getMessage());
        }
    }

    /**
     * Obtém metadados da foto sem carregar os dados binários
     * 
     * @param userId ID do usuário
     * @return Optional com metadados da foto
     */
    public Optional<UserPhoto> getPhotoMetadata(UUID userId) {
        return userPhotoRepository.findMetadataByUserId(userId);
    }

    /**
     * Calcula estatísticas de armazenamento de fotos
     * 
     * @return informações sobre uso de storage
     */
    public PhotoStorageStats getStorageStats() {
        long totalPhotos = userPhotoRepository.countAllPhotos();
        long totalStorage = userPhotoRepository.getTotalStorageUsed();

        return new PhotoStorageStats(totalPhotos, totalStorage);
    }

    /**
     * Valida se o arquivo enviado é válido para upload
     * 
     * @param file arquivo a ser validado
     * @return mensagem de erro ou null se válido
     */
    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "Arquivo não pode estar vazio";
        }

        if (file.getSize() > maxFileSize) {
            return String.format("Arquivo muito grande. Tamanho máximo: %d MB", maxFileSize / (1024 * 1024));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return "Tipo de arquivo não permitido. Use: JPEG, PNG ou GIF";
        }

        return null; // Arquivo válido
    }

    /**
     * Gera URL para acessar a foto
     * 
     * @param photoId ID da foto
     * @return URL da foto
     */
    private String generatePhotoUrl(UUID photoId) {
        return "/api/v1/user/photo/" + photoId;
    }

    /**
     * Classe interna para estatísticas de armazenamento
     */
    public static class PhotoStorageStats {
        private final long totalPhotos;
        private final long totalStorageBytes;

        public PhotoStorageStats(long totalPhotos, long totalStorageBytes) {
            this.totalPhotos = totalPhotos;
            this.totalStorageBytes = totalStorageBytes;
        }

        /**
         * Número total de fotos armazenadas
         * 
         * @return total de fotos
         */
        public long getTotalPhotos() {
            return totalPhotos;
        }

        /**
         * Espaço total ocupado em bytes
         * 
         * @return bytes utilizados
         */
        public long getTotalStorageBytes() {
            return totalStorageBytes;
        }

        /**
         * Espaço total ocupado em MB
         * 
         * @return megabytes utilizados
         */
        public double getTotalStorageMB() {
            return totalStorageBytes / (1024.0 * 1024.0);
        }
    }
}
