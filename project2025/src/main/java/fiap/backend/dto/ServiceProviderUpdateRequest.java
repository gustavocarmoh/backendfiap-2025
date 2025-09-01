package fiap.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de atualização de fornecedor de serviços.
 * Permite atualização parcial dos dados do fornecedor.
 */
public class ServiceProviderUpdateRequest {

    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String name;

    private Double longitude;

    private Double latitude;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    private Boolean active;

    @Size(max = 300, message = "Endereço deve ter no máximo 300 caracteres")
    private String endereco;

    /**
     * Construtor padrão
     */
    public ServiceProviderUpdateRequest() {
    }

    /**
     * Construtor completo para atualização
     * 
     * @param name        nome do fornecedor
     * @param longitude   longitude geográfica
     * @param latitude    latitude geográfica
     * @param description descrição dos serviços
     * @param active      status ativo/inativo
     * @param endereco    endereço completo
     */
    public ServiceProviderUpdateRequest(String name, Double longitude, Double latitude,
            String description, Boolean active, String endereco) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description = description;
        this.active = active;
        this.endereco = endereco;
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
     * Obtém a descrição dos serviços
     * 
     * @return descrição dos serviços oferecidos
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtém o status ativo/inativo
     * 
     * @return true se ativo, false se inativo
     */
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Obtém o endereço completo
     * 
     * @return endereço formatado
     */
    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "ServiceProviderUpdateRequest{" +
                "name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", active=" + active +
                ", endereco='" + endereco + '\'' +
                '}';
    }
}
