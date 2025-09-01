package fiap.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de upload de foto de perfil do usuário.
 * Contém validações para garantir que apenas arquivos válidos sejam aceitos.
 */
public class UserPhotoUploadRequest {

    @NotNull(message = "O arquivo da foto não pode ser nulo")
    private byte[] photoData;

    @Size(max = 100, message = "O nome do arquivo não pode exceder 100 caracteres")
    private String fileName;

    @Size(max = 50, message = "O tipo do arquivo não pode exceder 50 caracteres")
    private String contentType;

    public UserPhotoUploadRequest() {
    }

    public UserPhotoUploadRequest(byte[] photoData, String fileName, String contentType) {
        this.photoData = photoData;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    /**
     * Obtém os dados binários da foto
     * 
     * @return array de bytes contendo a imagem
     */
    public byte[] getPhotoData() {
        return photoData;
    }

    /**
     * Define os dados binários da foto
     * 
     * @param photoData array de bytes da imagem
     */
    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
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
     * Obtém o tipo MIME do arquivo
     * 
     * @return tipo de conteúdo (ex: image/jpeg, image/png)
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Define o tipo MIME do arquivo
     * 
     * @param contentType tipo de conteúdo
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Verifica se o tipo de arquivo é uma imagem válida
     * 
     * @return true se for uma imagem válida
     */
    public boolean isValidImageType() {
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"));
    }
}
