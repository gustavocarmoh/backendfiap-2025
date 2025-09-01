package fiap.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um fornecedor de serviços.
 * Contém informações geográficas para plotagem em mapas e dados
 * administrativos.
 */
@Entity
@Table(name = "service_providers")
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @NotNull
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Size(max = 500)
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotBlank
    @Size(max = 300)
    @Column(name = "endereco", nullable = false)
    private String endereco;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    /**
     * Construtor padrão necessário para JPA
     */
    public ServiceProvider() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * Construtor para criação de novo fornecedor
     * 
     * @param name        nome do fornecedor
     * @param longitude   longitude geográfica
     * @param latitude    latitude geográfica
     * @param description descrição dos serviços
     * @param endereco    endereço completo
     * @param createdBy   ID do administrador que criou
     */
    public ServiceProvider(String name, Double longitude, Double latitude,
            String description, String endereco, UUID createdBy) {
        this();
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description = description;
        this.endereco = endereco;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    /**
     * Atualiza o timestamp de modificação
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters e Setters

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
     * Obtém a longitude geográfica
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
     * Obtém a latitude geográfica
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
        return "ServiceProvider{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", active=" + active +
                ", endereco='" + endereco + '\'' +
                '}';
    }
}
