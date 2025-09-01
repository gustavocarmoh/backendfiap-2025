package fiap.backend.dto;

import fiap.backend.domain.ServiceProvider;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de operações com fornecedores de serviços.
 * Contém todos os dados do fornecedor otimizados para consumo pelo frontend,
 * especialmente para plotagem em mapas.
 */
public class ServiceProviderResponse {

    private UUID id;
    private String name;
    private Double longitude;
    private Double latitude;
    private String description;
    private Boolean active;
    private String endereco;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    /**
     * Construtor padrão
     */
    public ServiceProviderResponse() {
    }

    /**
     * Construtor que converte entidade em DTO
     * 
     * @param serviceProvider entidade do fornecedor
     */
    public ServiceProviderResponse(ServiceProvider serviceProvider) {
        this.id = serviceProvider.getId();
        this.name = serviceProvider.getName();
        this.longitude = serviceProvider.getLongitude();
        this.latitude = serviceProvider.getLatitude();
        this.description = serviceProvider.getDescription();
        this.active = serviceProvider.getActive();
        this.endereco = serviceProvider.getEndereco();
        this.createdAt = serviceProvider.getCreatedAt();
        this.updatedAt = serviceProvider.getUpdatedAt();
        this.createdBy = serviceProvider.getCreatedBy();
        this.updatedBy = serviceProvider.getUpdatedBy();
    }

    /**
     * Método estático para criar resposta de sucesso
     * 
     * @param serviceProvider fornecedor criado/atualizado
     * @return DTO de resposta
     */
    public static ServiceProviderResponse success(ServiceProvider serviceProvider) {
        return new ServiceProviderResponse(serviceProvider);
    }

    /**
     * Obtém o ID único do fornecedor
     * 
     * @return UUID do fornecedor
     */
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Obtém o nome do fornecedor
     * 
     * @return nome do fornecedor
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtém a longitude geográfica para plotagem no mapa
     * 
     * @return longitude em formato decimal
     */
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Obtém a latitude geográfica para plotagem no mapa
     * 
     * @return latitude em formato decimal
     */
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Obtém a descrição dos serviços oferecidos
     * 
     * @return descrição do fornecedor
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Verifica se o fornecedor está ativo
     * 
     * @return true se ativo, false caso contrário
     */
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Obtém o endereço completo do fornecedor
     * 
     * @return endereço formatado
     */
    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    /**
     * Obtém a data de criação
     * 
     * @return timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtém a data da última atualização
     * 
     * @return timestamp de atualização
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Obtém o ID do usuário que criou o fornecedor
     * 
     * @return UUID do criador
     */
    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Obtém o ID do usuário que fez a última atualização
     * 
     * @return UUID do usuário que atualizou
     */
    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "ServiceProviderResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", active=" + active +
                ", endereco='" + endereco + '\'' +
                '}';
    }
}
