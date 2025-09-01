package fiap.backend.service;

import fiap.backend.domain.ServiceProvider;
import fiap.backend.dto.ServiceProviderCreateRequest;
import fiap.backend.dto.ServiceProviderResponse;
import fiap.backend.dto.ServiceProviderUpdateRequest;
import fiap.backend.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o ServiceProviderService.
 * Verifica funcionalidades CRUD e validações para fornecedores de serviços.
 */
@ExtendWith(MockitoExtension.class)
class ServiceProviderServiceTest {

    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @InjectMocks
    private ServiceProviderService serviceProviderService;

    private UUID adminId;
    private ServiceProvider testProvider;
    private ServiceProviderCreateRequest createRequest;
    private ServiceProviderUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();

        // Setup de fornecedor de teste
        testProvider = new ServiceProvider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setName("TechFix Assistência");
        testProvider.setLongitude(-46.633308);
        testProvider.setLatitude(-23.550520);
        testProvider.setDescription("Assistência técnica especializada");
        testProvider.setEndereco("Rua Augusta, 123 - São Paulo - SP");
        testProvider.setActive(true);
        testProvider.setCreatedBy(adminId);

        // Setup de request de criação
        createRequest = new ServiceProviderCreateRequest(
                "Novo Fornecedor",
                -46.654295,
                -23.563986,
                "Descrição do serviço",
                "Av. Paulista, 456 - São Paulo - SP");

        // Setup de request de atualização
        updateRequest = new ServiceProviderUpdateRequest();
        updateRequest.setName("Nome Atualizado");
        updateRequest.setDescription("Descrição atualizada");
        updateRequest.setActive(false);
    }

    @Test
    void createServiceProvider_Success() {
        // Arrange
        when(serviceProviderRepository.existsByNameIgnoreCase(createRequest.getName())).thenReturn(false);
        when(serviceProviderRepository.save(any(ServiceProvider.class))).thenReturn(testProvider);

        // Act
        ServiceProviderResponse response = serviceProviderService.createServiceProvider(createRequest, adminId);

        // Assert
        assertNotNull(response);
        assertEquals(testProvider.getId(), response.getId());
        assertEquals(testProvider.getName(), response.getName());
        assertEquals(testProvider.getLongitude(), response.getLongitude());
        assertEquals(testProvider.getLatitude(), response.getLatitude());
        assertTrue(response.getActive());

        verify(serviceProviderRepository).existsByNameIgnoreCase(createRequest.getName());
        verify(serviceProviderRepository).save(any(ServiceProvider.class));
    }

    @Test
    void createServiceProvider_NameAlreadyExists_ThrowsException() {
        // Arrange
        when(serviceProviderRepository.existsByNameIgnoreCase(createRequest.getName())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceProviderService.createServiceProvider(createRequest, adminId));
        assertEquals("Já existe um fornecedor com este nome", exception.getMessage());

        verify(serviceProviderRepository).existsByNameIgnoreCase(createRequest.getName());
        verify(serviceProviderRepository, never()).save(any());
    }

    @Test
    void createServiceProvider_InvalidCoordinates_ThrowsException() {
        // Arrange
        createRequest.setLongitude(200.0); // Longitude inválida
        when(serviceProviderRepository.existsByNameIgnoreCase(createRequest.getName())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceProviderService.createServiceProvider(createRequest, adminId));
        assertTrue(exception.getMessage().contains("Longitude deve estar entre -180 e 180"));

        verify(serviceProviderRepository, never()).save(any());
    }

    @Test
    void updateServiceProvider_Success() {
        // Arrange
        UUID providerId = testProvider.getId();
        when(serviceProviderRepository.findById(providerId)).thenReturn(Optional.of(testProvider));
        when(serviceProviderRepository.existsByNameIgnoreCaseAndIdNot(updateRequest.getName(), providerId))
                .thenReturn(false);
        when(serviceProviderRepository.save(any(ServiceProvider.class))).thenReturn(testProvider);

        // Act
        ServiceProviderResponse response = serviceProviderService.updateServiceProvider(providerId, updateRequest,
                adminId);

        // Assert
        assertNotNull(response);
        verify(serviceProviderRepository).findById(providerId);
        verify(serviceProviderRepository).save(any(ServiceProvider.class));
    }

    @Test
    void updateServiceProvider_NotFound_ThrowsException() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(serviceProviderRepository.findById(providerId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceProviderService.updateServiceProvider(providerId, updateRequest, adminId));
        assertEquals("Fornecedor não encontrado", exception.getMessage());

        verify(serviceProviderRepository).findById(providerId);
        verify(serviceProviderRepository, never()).save(any());
    }

    @Test
    void deactivateServiceProvider_Success() {
        // Arrange
        UUID providerId = testProvider.getId();
        when(serviceProviderRepository.findById(providerId)).thenReturn(Optional.of(testProvider));
        when(serviceProviderRepository.save(any(ServiceProvider.class))).thenReturn(testProvider);

        // Act
        assertDoesNotThrow(() -> serviceProviderService.deactivateServiceProvider(providerId, adminId));

        // Assert
        verify(serviceProviderRepository).findById(providerId);
        verify(serviceProviderRepository).save(any(ServiceProvider.class));
    }

    @Test
    void getAllActiveProviders_Success() {
        // Arrange
        List<ServiceProvider> activeProviders = Arrays.asList(testProvider);
        when(serviceProviderRepository.findAllActiveOrderByName()).thenReturn(activeProviders);

        // Act
        List<ServiceProviderResponse> responses = serviceProviderService.getAllActiveProviders();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testProvider.getId(), responses.get(0).getId());
        assertEquals(testProvider.getName(), responses.get(0).getName());

        verify(serviceProviderRepository).findAllActiveOrderByName();
    }

    @Test
    void getProviderById_Success() {
        // Arrange
        UUID providerId = testProvider.getId();
        when(serviceProviderRepository.findById(providerId)).thenReturn(Optional.of(testProvider));

        // Act
        Optional<ServiceProviderResponse> response = serviceProviderService.getProviderById(providerId);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(testProvider.getId(), response.get().getId());
        assertEquals(testProvider.getName(), response.get().getName());

        verify(serviceProviderRepository).findById(providerId);
    }

    @Test
    void getProviderById_NotFound() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(serviceProviderRepository.findById(providerId)).thenReturn(Optional.empty());

        // Act
        Optional<ServiceProviderResponse> response = serviceProviderService.getProviderById(providerId);

        // Assert
        assertFalse(response.isPresent());
        verify(serviceProviderRepository).findById(providerId);
    }

    @Test
    void searchActiveProvidersByName_Success() {
        // Arrange
        String searchName = "Tech";
        List<ServiceProvider> foundProviders = Arrays.asList(testProvider);
        when(serviceProviderRepository.findActiveByNameContainingIgnoreCase(searchName))
                .thenReturn(foundProviders);

        // Act
        List<ServiceProviderResponse> responses = serviceProviderService.searchActiveProvidersByName(searchName);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testProvider.getName(), responses.get(0).getName());

        verify(serviceProviderRepository).findActiveByNameContainingIgnoreCase(searchName);
    }

    @Test
    void getProvidersByLocation_Success() {
        // Arrange
        Double minLat = -23.6, maxLat = -23.5, minLng = -46.7, maxLng = -46.6;
        List<ServiceProvider> providersInArea = Arrays.asList(testProvider);
        when(serviceProviderRepository.findByLocationBounds(minLat, maxLat, minLng, maxLng))
                .thenReturn(providersInArea);

        // Act
        List<ServiceProviderResponse> responses = serviceProviderService.getProvidersByLocation(
                minLat, maxLat, minLng, maxLng);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testProvider.getId(), responses.get(0).getId());

        verify(serviceProviderRepository).findByLocationBounds(minLat, maxLat, minLng, maxLng);
    }

    @Test
    void getProvidersByLocation_InvalidCoordinates_ThrowsException() {
        // Arrange
        Double invalidLat = 100.0; // Latitude inválida

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceProviderService.getProvidersByLocation(-23.6, invalidLat, -46.7, -46.6));
        assertTrue(exception.getMessage().contains("Latitude deve estar entre -90 e 90"));

        verify(serviceProviderRepository, never()).findByLocationBounds(any(), any(), any(), any());
    }

    @Test
    void getProviderStats_Success() {
        // Arrange
        Object[] statsData = { 5L, 4L, 1L }; // total, ativo, inativo
        when(serviceProviderRepository.getBasicStats()).thenReturn(statsData);

        // Act
        ServiceProviderService.ProviderStats stats = serviceProviderService.getProviderStats();

        // Assert
        assertNotNull(stats);
        assertEquals(5L, stats.getTotal());
        assertEquals(4L, stats.getActive());
        assertEquals(1L, stats.getInactive());

        verify(serviceProviderRepository).getBasicStats();
    }

    @Test
    void deleteServiceProvider_Success() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(serviceProviderRepository.existsById(providerId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> serviceProviderService.deleteServiceProvider(providerId));

        // Assert
        verify(serviceProviderRepository).existsById(providerId);
        verify(serviceProviderRepository).deleteById(providerId);
    }

    @Test
    void deleteServiceProvider_NotFound_ThrowsException() {
        // Arrange
        UUID providerId = UUID.randomUUID();
        when(serviceProviderRepository.existsById(providerId)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> serviceProviderService.deleteServiceProvider(providerId));
        assertEquals("Fornecedor não encontrado", exception.getMessage());

        verify(serviceProviderRepository).existsById(providerId);
        verify(serviceProviderRepository, never()).deleteById(any());
    }
}
