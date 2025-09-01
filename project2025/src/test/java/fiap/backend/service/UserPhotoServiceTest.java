package fiap.backend.service;

import fiap.backend.domain.User;
import fiap.backend.domain.UserPhoto;
import fiap.backend.dto.UserPhotoResponse;
import fiap.backend.repository.UserPhotoRepository;
import fiap.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o serviço de fotos de usuário.
 * Verifica funcionalidades de upload, recuperação e remoção de fotos.
 */
@ExtendWith(MockitoExtension.class)
class UserPhotoServiceTest {

    @Mock
    private UserPhotoRepository userPhotoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPhotoService userPhotoService;

    private User testUser;
    private UUID testUserId;
    private UserPhoto testPhoto;
    private MultipartFile validImageFile;
    private byte[] testImageData;

    @BeforeEach
    void setUp() {
        // Configurar dados de teste
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");

        testImageData = "fake_image_data".getBytes();
        testPhoto = new UserPhoto(testUser, testImageData, "test.jpg", "image/jpeg");
        testPhoto.setId(UUID.randomUUID());

        validImageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                testImageData);
    }

    /**
     * Teste de upload bem-sucedido de nova foto
     */
    @Test
    void testUploadUserPhoto_Success_NewPhoto() throws IOException {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userPhotoRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(userPhotoRepository.save(any(UserPhoto.class))).thenReturn(testPhoto);

        // Act
        UserPhotoResponse response = userPhotoService.uploadUserPhoto(testUserId, validImageFile);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Foto de perfil atualizada com sucesso", response.getMessage());
        assertNotNull(response.getPhotoUrl());
        verify(userPhotoRepository).save(any(UserPhoto.class));
        verify(userRepository).save(testUser);
    }

    /**
     * Teste de upload bem-sucedido atualizando foto existente
     */
    @Test
    void testUploadUserPhoto_Success_UpdateExisting() throws IOException {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userPhotoRepository.findByUser(testUser)).thenReturn(Optional.of(testPhoto));
        when(userPhotoRepository.save(any(UserPhoto.class))).thenReturn(testPhoto);

        // Act
        UserPhotoResponse response = userPhotoService.uploadUserPhoto(testUserId, validImageFile);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Foto de perfil atualizada com sucesso", response.getMessage());
        verify(userPhotoRepository).save(testPhoto);
    }

    /**
     * Teste de falha quando usuário não é encontrado
     */
    @Test
    void testUploadUserPhoto_UserNotFound() throws IOException {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userPhotoService.uploadUserPhoto(testUserId, validImageFile);
        });
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    /**
     * Teste de falha com arquivo muito grande
     */
    @Test
    void testUploadUserPhoto_FileTooLarge() throws IOException {
        // Arrange
        byte[] largeFileData = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeFileData);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        UserPhotoResponse response = userPhotoService.uploadUserPhoto(testUserId, largeFile);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("muito grande"));
    }

    /**
     * Teste de falha com tipo de arquivo inválido
     */
    @Test
    void testUploadUserPhoto_InvalidFileType() throws IOException {
        // Arrange
        MultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not an image".getBytes());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        UserPhotoResponse response = userPhotoService.uploadUserPhoto(testUserId, invalidFile);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("não permitido"));
    }

    /**
     * Teste de recuperação bem-sucedida de foto
     */
    @Test
    void testGetUserPhoto_Success() {
        // Arrange
        when(userPhotoRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPhoto));

        // Act
        Optional<UserPhoto> result = userPhotoService.getUserPhoto(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPhoto, result.get());
    }

    /**
     * Teste de recuperação quando não há foto
     */
    @Test
    void testGetUserPhoto_NotFound() {
        // Arrange
        when(userPhotoRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // Act
        Optional<UserPhoto> result = userPhotoService.getUserPhoto(testUserId);

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Teste de verificação se usuário tem foto
     */
    @Test
    void testUserHasPhoto_True() {
        // Arrange
        when(userPhotoRepository.existsByUserId(testUserId)).thenReturn(true);

        // Act
        boolean result = userPhotoService.userHasPhoto(testUserId);

        // Assert
        assertTrue(result);
    }

    /**
     * Teste de verificação quando usuário não tem foto
     */
    @Test
    void testUserHasPhoto_False() {
        // Arrange
        when(userPhotoRepository.existsByUserId(testUserId)).thenReturn(false);

        // Act
        boolean result = userPhotoService.userHasPhoto(testUserId);

        // Assert
        assertFalse(result);
    }

    /**
     * Teste de remoção bem-sucedida de foto
     */
    @Test
    void testDeleteUserPhoto_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userPhotoRepository.existsByUserId(testUserId)).thenReturn(true);

        // Act
        UserPhotoResponse response = userPhotoService.deleteUserPhoto(testUserId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Foto de perfil removida com sucesso", response.getMessage());
        verify(userPhotoRepository).deleteByUserId(testUserId);
        verify(userRepository).save(testUser);
        assertNull(testUser.getProfilePhotoUrl());
    }

    /**
     * Teste de falha na remoção quando não há foto
     */
    @Test
    void testDeleteUserPhoto_NoPhoto() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userPhotoRepository.existsByUserId(testUserId)).thenReturn(false);

        // Act
        UserPhotoResponse response = userPhotoService.deleteUserPhoto(testUserId);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Usuário não possui foto de perfil", response.getMessage());
    }

    /**
     * Teste de obtenção de estatísticas de armazenamento
     */
    @Test
    void testGetStorageStats() {
        // Arrange
        when(userPhotoRepository.countAllPhotos()).thenReturn(10L);
        when(userPhotoRepository.getTotalStorageUsed()).thenReturn(1048576L); // 1MB

        // Act
        UserPhotoService.PhotoStorageStats stats = userPhotoService.getStorageStats();

        // Assert
        assertEquals(10L, stats.getTotalPhotos());
        assertEquals(1048576L, stats.getTotalStorageBytes());
        assertEquals(1.0, stats.getTotalStorageMB(), 0.01);
    }

    /**
     * Teste de recuperação de foto pelo ID
     */
    @Test
    void testGetPhotoById_Success() {
        // Arrange
        UUID photoId = testPhoto.getId();
        when(userPhotoRepository.findById(photoId)).thenReturn(Optional.of(testPhoto));

        // Act
        Optional<UserPhoto> result = userPhotoService.getPhotoById(photoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPhoto, result.get());
    }

    /**
     * Teste de recuperação de metadados da foto
     */
    @Test
    void testGetPhotoMetadata_Success() {
        // Arrange
        when(userPhotoRepository.findMetadataByUserId(testUserId)).thenReturn(Optional.of(testPhoto));

        // Act
        Optional<UserPhoto> result = userPhotoService.getPhotoMetadata(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPhoto, result.get());
    }
}
