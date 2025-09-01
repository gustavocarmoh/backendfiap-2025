package fiap.backend.service;

import fiap.backend.domain.ServiceProvider;
import fiap.backend.dto.ServiceProviderCreateRequest;
import fiap.backend.dto.ServiceProviderResponse;
import fiap.backend.dto.ServiceProviderUpdateRequest;
import fiap.backend.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de fornecedores de serviços.
 * Implementa regras de negócio e validações para operações CRUD restritas a
 * administradores.
 */
@Service
@Transactional
public class ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    /**
     * Cria um novo fornecedor de serviços (apenas administradores)
     * 
     * @param request dados do fornecedor
     * @param adminId ID do administrador que está criando
     * @return DTO de resposta com o fornecedor criado
     * @throws RuntimeException se nome já existir ou dados inválidos
     */
    public ServiceProviderResponse createServiceProvider(ServiceProviderCreateRequest request, UUID adminId) {
        // Validar se nome já existe
        if (serviceProviderRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Já existe um fornecedor com este nome");
        }

        // Validar coordenadas geográficas
        validateGeographicCoordinates(request.getLongitude(), request.getLatitude());

        try {
            // Criar nova entidade
            ServiceProvider serviceProvider = new ServiceProvider(
                    request.getName(),
                    request.getLongitude(),
                    request.getLatitude(),
                    request.getDescription(),
                    request.getEndereco(),
                    adminId);

            // Salvar no banco
            ServiceProvider savedProvider = serviceProviderRepository.save(serviceProvider);

            return ServiceProviderResponse.success(savedProvider);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar fornecedor: " + e.getMessage());
        }
    }

    /**
     * Atualiza um fornecedor existente (apenas administradores)
     * 
     * @param id      ID do fornecedor
     * @param request dados para atualização
     * @param adminId ID do administrador que está atualizando
     * @return DTO de resposta com o fornecedor atualizado
     * @throws RuntimeException se fornecedor não encontrado ou dados inválidos
     */
    public ServiceProviderResponse updateServiceProvider(UUID id, ServiceProviderUpdateRequest request, UUID adminId) {
        // Buscar fornecedor existente
        ServiceProvider existingProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        // Validar se novo nome já existe (excluindo o próprio fornecedor)
        if (request.getName() != null && !request.getName().equals(existingProvider.getName())) {
            if (serviceProviderRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new RuntimeException("Já existe outro fornecedor com este nome");
            }
        }

        // Validar coordenadas se fornecidas
        if (request.getLongitude() != null && request.getLatitude() != null) {
            validateGeographicCoordinates(request.getLongitude(), request.getLatitude());
        }

        try {
            // Atualizar campos fornecidos (patch update)
            updateProviderFields(existingProvider, request, adminId);

            // Salvar alterações
            ServiceProvider updatedProvider = serviceProviderRepository.save(existingProvider);

            return ServiceProviderResponse.success(updatedProvider);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar fornecedor: " + e.getMessage());
        }
    }

    /**
     * Remove um fornecedor (soft delete - marca como inativo)
     * 
     * @param id      ID do fornecedor
     * @param adminId ID do administrador que está removendo
     * @throws RuntimeException se fornecedor não encontrado
     */
    public void deactivateServiceProvider(UUID id, UUID adminId) {
        ServiceProvider provider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        provider.setActive(false);
        provider.setUpdatedBy(adminId);
        provider.setUpdatedAt(LocalDateTime.now());

        serviceProviderRepository.save(provider);
    }

    /**
     * Remove permanentemente um fornecedor (hard delete)
     * 
     * @param id ID do fornecedor
     * @throws RuntimeException se fornecedor não encontrado
     */
    public void deleteServiceProvider(UUID id) {
        if (!serviceProviderRepository.existsById(id)) {
            throw new RuntimeException("Fornecedor não encontrado");
        }

        serviceProviderRepository.deleteById(id);
    }

    /**
     * Busca todos os fornecedores ativos (público)
     * 
     * @return lista de fornecedores ativos
     */
    @Transactional(readOnly = true)
    public List<ServiceProviderResponse> getAllActiveProviders() {
        return serviceProviderRepository.findAllActiveOrderByName()
                .stream()
                .map(ServiceProviderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os fornecedores (ativos e inativos) - apenas administradores
     * 
     * @return lista completa de fornecedores
     */
    @Transactional(readOnly = true)
    public List<ServiceProviderResponse> getAllProviders() {
        return serviceProviderRepository.findAllOrderByName()
                .stream()
                .map(ServiceProviderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca fornecedores por status (ativo/inativo)
     * 
     * @param active status desejado (true para ativos, false para inativos)
     * @return lista de fornecedores com o status especificado
     */
    @Transactional(readOnly = true)
    public List<ServiceProviderResponse> getProvidersByStatus(boolean active) {
        return serviceProviderRepository.findByActiveOrderByName(active)
                .stream()
                .map(ServiceProviderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca fornecedor por ID
     * 
     * @param id ID do fornecedor
     * @return DTO do fornecedor ou Optional vazio
     */
    @Transactional(readOnly = true)
    public Optional<ServiceProviderResponse> getProviderById(UUID id) {
        return serviceProviderRepository.findById(id)
                .map(ServiceProviderResponse::new);
    }

    /**
     * Busca fornecedores ativos por nome
     * 
     * @param name parte do nome para buscar
     * @return lista de fornecedores que correspondem ao nome
     */
    @Transactional(readOnly = true)
    public List<ServiceProviderResponse> searchActiveProvidersByName(String name) {
        return serviceProviderRepository.findActiveByNameContainingIgnoreCase(name)
                .stream()
                .map(ServiceProviderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca fornecedores dentro de uma área geográfica
     * 
     * @param minLat latitude mínima
     * @param maxLat latitude máxima
     * @param minLng longitude mínima
     * @param maxLng longitude máxima
     * @return lista de fornecedores na área especificada
     */
    @Transactional(readOnly = true)
    public List<ServiceProviderResponse> getProvidersByLocation(Double minLat, Double maxLat,
            Double minLng, Double maxLng) {
        validateGeographicCoordinates(minLng, minLat);
        validateGeographicCoordinates(maxLng, maxLat);

        return serviceProviderRepository.findByLocationBounds(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(ServiceProviderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtém estatísticas dos fornecedores
     * 
     * @return mapa com estatísticas
     */
    @Transactional(readOnly = true)
    public ProviderStats getProviderStats() {
        Object[] stats = serviceProviderRepository.getBasicStats();

        long total = ((Number) stats[0]).longValue();
        long active = ((Number) stats[1]).longValue();
        long inactive = ((Number) stats[2]).longValue();

        return new ProviderStats(total, active, inactive);
    }

    /**
     * Valida coordenadas geográficas
     * 
     * @param longitude longitude a validar
     * @param latitude  latitude a validar
     * @throws RuntimeException se coordenadas inválidas
     */
    private void validateGeographicCoordinates(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            throw new RuntimeException("Longitude e latitude são obrigatórias");
        }

        if (longitude < -180.0 || longitude > 180.0) {
            throw new RuntimeException("Longitude deve estar entre -180 e 180 graus");
        }

        if (latitude < -90.0 || latitude > 90.0) {
            throw new RuntimeException("Latitude deve estar entre -90 e 90 graus");
        }
    }

    /**
     * Atualiza campos do fornecedor com os dados fornecidos
     * 
     * @param provider fornecedor a ser atualizado
     * @param request  dados de atualização
     * @param adminId  ID do administrador
     */
    private void updateProviderFields(ServiceProvider provider, ServiceProviderUpdateRequest request, UUID adminId) {
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            provider.setName(request.getName().trim());
        }

        if (request.getLongitude() != null) {
            provider.setLongitude(request.getLongitude());
        }

        if (request.getLatitude() != null) {
            provider.setLatitude(request.getLatitude());
        }

        if (request.getDescription() != null) {
            provider.setDescription(request.getDescription().trim());
        }

        if (request.getActive() != null) {
            provider.setActive(request.getActive());
        }

        if (request.getEndereco() != null && !request.getEndereco().trim().isEmpty()) {
            provider.setEndereco(request.getEndereco().trim());
        }

        provider.setUpdatedBy(adminId);
        provider.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Classe para estatísticas de fornecedores
     */
    public static class ProviderStats {
        private final long total;
        private final long active;
        private final long inactive;

        public ProviderStats(long total, long active, long inactive) {
            this.total = total;
            this.active = active;
            this.inactive = inactive;
        }

        /**
         * Número total de fornecedores
         * 
         * @return total de fornecedores
         */
        public long getTotal() {
            return total;
        }

        /**
         * Número de fornecedores ativos
         * 
         * @return fornecedores ativos
         */
        public long getActive() {
            return active;
        }

        /**
         * Número de fornecedores inativos
         * 
         * @return fornecedores inativos
         */
        public long getInactive() {
            return inactive;
        }
    }
}
