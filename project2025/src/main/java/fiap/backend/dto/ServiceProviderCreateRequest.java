package fiap.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de criação de fornecedor de serviços.
 * Contém os dados necessários para registrar um novo fornecedor no sistema.
 */
public class ServiceProviderCreateRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String name;

    @NotNull(message = "Longitude é obrigatória")
    private Double longitude;

    @NotNull(message = "Latitude é obrigatória")
    private Double latitude;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 300, message = "Endereço deve ter no máximo 300 caracteres")
    private String endereco;

    /**
     * Construtor padrão
     */
    public ServiceProviderCreateRequest() {
    }

    /**
     * Construtor completo para criação de fornecedor
     * 
     * @param name        nome do fornecedor
     * @param longitude   longitude geográfica
     * @param latitude    latitude geográfica
     * @param description descrição dos serviços
     * @param endereco    endereço completo
     */
    public ServiceProviderCreateRequest(String name, Double longitude, Double latitude,
            String description, String endereco) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description = description;
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
        return "ServiceProviderCreateRequest{" +
                "name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", endereco='" + endereco + '\'' +
                '}';
    }
}
