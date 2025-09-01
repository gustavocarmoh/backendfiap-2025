package fiap.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade para armazenar fotos de perfil dos usuários no banco de dados.
 * Mantém os dados binários da imagem e metadados associados.
 */
@Entity
@Table(name = "user_photos")
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Lob
    @Column(name = "photo_data", nullable = false)
    private byte[] photoData;

    @Size(max = 100)
    @Column(name = "file_name")
    private String fileName;

    @Size(max = 50)
    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserPhoto() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserPhoto(User user, byte[] photoData, String fileName, String contentType) {
        this();
        this.user = user;
        this.photoData = photoData;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = photoData != null ? (long) photoData.length : 0L;
    }

    /**
     * Obtém o ID único da foto
     * 
     * @return UUID da foto
     */
    public UUID getId() {
        return id;
    }

    /**
     * Define o ID da foto
     * 
     * @param id UUID da foto
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Obtém o usuário proprietário da foto
     * 
     * @return entidade User
     */
    public User getUser() {
        return user;
    }

    /**
     * Define o usuário proprietário da foto
     * 
     * @param user entidade User
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Obtém os dados binários da foto
     * 
     * @return array de bytes da imagem
     */
    public byte[] getPhotoData() {
        return photoData;
    }

    /**
     * Define os dados binários da foto e atualiza o tamanho
     * 
     * @param photoData array de bytes da imagem
     */
    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
        this.fileSize = photoData != null ? (long) photoData.length : 0L;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Obtém o nome original do arquivo
     * 
     * @return nome do arquivo
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Define o nome do arquivo
     * 
     * @param fileName nome do arquivo
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Obtém o tipo MIME da imagem
     * 
     * @return tipo de conteúdo
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Define o tipo MIME da imagem
     * 
     * @param contentType tipo de conteúdo
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Obtém o tamanho do arquivo em bytes
     * 
     * @return tamanho do arquivo
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Define o tamanho do arquivo
     * 
     * @param fileSize tamanho em bytes
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Obtém a data de criação da foto
     * 
     * @return data e hora de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data de criação
     * 
     * @param createdAt data e hora de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtém a data da última atualização
     * 
     * @return data e hora da última atualização
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Define a data de atualização
     * 
     * @param updatedAt data e hora de atualização
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Atualiza a timestamp de modificação antes de salvar
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se a foto é uma imagem válida baseada no tipo de conteúdo
     * 
     * @return true se for uma imagem válida
     */
    public boolean isValidImage() {
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"));
    }
}
