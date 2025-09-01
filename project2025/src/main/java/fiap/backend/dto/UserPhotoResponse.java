package fiap.backend.dto;

/**
 * DTO para resposta de operações com foto de perfil do usuário.
 * Retorna informações sobre o status do upload e URL da foto.
 */
public class UserPhotoResponse {

    private String photoUrl;
    private String message;
    private boolean success;
    private Long fileSize;
    private String contentType;

    public UserPhotoResponse() {
    }

    public UserPhotoResponse(String photoUrl, String message, boolean success) {
        this.photoUrl = photoUrl;
        this.message = message;
        this.success = success;
    }

    public UserPhotoResponse(String photoUrl, String message, boolean success, Long fileSize, String contentType) {
        this.photoUrl = photoUrl;
        this.message = message;
        this.success = success;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    /**
     * Obtém a URL da foto de perfil
     * 
     * @return URL para acessar a foto
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Define a URL da foto de perfil
     * 
     * @param photoUrl URL da foto
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * Obtém a mensagem de status da operação
     * 
     * @return mensagem descritiva
     */
    public String getMessage() {
        return message;
    }

    /**
     * Define a mensagem de status
     * 
     * @param message mensagem a ser exibida
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Verifica se a operação foi bem-sucedida
     * 
     * @return true se sucesso, false caso contrário
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Define o status de sucesso da operação
     * 
     * @param success status da operação
     */
    public void setSuccess(boolean success) {
        this.success = success;
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
     * Obtém o tipo MIME do arquivo
     * 
     * @return tipo de conteúdo
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
     * Cria uma resposta de sucesso
     * 
     * @param photoUrl URL da foto
     * @param message  mensagem de sucesso
     * @return instância de UserPhotoResponse
     */
    public static UserPhotoResponse success(String photoUrl, String message) {
        return new UserPhotoResponse(photoUrl, message, true);
    }

    /**
     * Cria uma resposta de erro
     * 
     * @param message mensagem de erro
     * @return instância de UserPhotoResponse
     */
    public static UserPhotoResponse error(String message) {
        return new UserPhotoResponse(null, message, false);
    }
}
