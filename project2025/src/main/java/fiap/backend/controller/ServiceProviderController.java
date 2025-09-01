package fiap.backend.controller;

import fiap.backend.dto.ServiceProviderCreateRequest;
import fiap.backend.dto.ServiceProviderResponse;
import fiap.backend.dto.ServiceProviderUpdateRequest;
import fiap.backend.service.CurrentUserService;
import fiap.backend.service.ServiceProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller responsável pelo gerenciamento de fornecedores de serviços.
 * Fornece endpoints administrativos (restritos) e públicos para consulta.
 * Dados otimizados para plotagem em mapas no frontend.
 */
@RestController
@RequestMapping("/api/v1/service-providers")
@Tag(name = "Service Providers", description = "Gerenciamento de fornecedores de serviços com dados geográficos")
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;
    private final CurrentUserService currentUserService;

    public ServiceProviderController(ServiceProviderService serviceProviderService,
            CurrentUserService currentUserService) {
        this.serviceProviderService = serviceProviderService;
        this.currentUserService = currentUserService;
    }

    /**
     * Cria um novo fornecedor de serviços (apenas administradores)
     * 
     * @param request dados do novo fornecedor
     * @return resposta com o fornecedor criado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar fornecedor (Admin)", description = "Registra um novo fornecedor de serviços no sistema. Requer privilégios de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceProviderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome já existe"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> createServiceProvider(@Valid @RequestBody ServiceProviderCreateRequest request) {
        try {
            UUID adminId = currentUserService.getCurrentUserId();
            ServiceProviderResponse response = serviceProviderService.createServiceProvider(request, adminId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Atualiza um fornecedor existente (apenas administradores)
     * 
     * @param id      ID do fornecedor
     * @param request dados para atualização
     * @return resposta com o fornecedor atualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar fornecedor (Admin)", description = "Atualiza dados de um fornecedor existente. Requer privilégios de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fornecedor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado")
    })
    public ResponseEntity<?> updateServiceProvider(
            @Parameter(description = "ID do fornecedor") @PathVariable UUID id,
            @Valid @RequestBody ServiceProviderUpdateRequest request) {
        try {
            UUID adminId = currentUserService.getCurrentUserId();
            ServiceProviderResponse response = serviceProviderService.updateServiceProvider(id, request, adminId);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Desativa um fornecedor (soft delete) - apenas administradores
     * 
     * @param id ID do fornecedor
     * @return resposta de confirmação
     */
    @DeleteMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar fornecedor (Admin)", description = "Marca um fornecedor como inativo sem removê-lo permanentemente.")
    public ResponseEntity<?> deactivateServiceProvider(
            @Parameter(description = "ID do fornecedor") @PathVariable UUID id) {
        try {
            UUID adminId = currentUserService.getCurrentUserId();
            serviceProviderService.deactivateServiceProvider(id, adminId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Fornecedor desativado com sucesso");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Remove permanentemente um fornecedor (apenas administradores)
     * 
     * @param id ID do fornecedor
     * @return resposta de confirmação
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover fornecedor permanentemente (Admin)", description = "Remove um fornecedor permanentemente do sistema.")
    public ResponseEntity<?> deleteServiceProvider(
            @Parameter(description = "ID do fornecedor") @PathVariable UUID id) {
        try {
            serviceProviderService.deleteServiceProvider(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Fornecedor removido permanentemente");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Lista fornecedores ativos (público)
     * 
     * @return lista de fornecedores ativos com dados para mapa
     */
    @GetMapping("/active")
    @Operation(summary = "Listar fornecedores ativos", description = "Retorna todos os fornecedores ativos com coordenadas para plotagem no mapa")
    @ApiResponse(responseCode = "200", description = "Lista de fornecedores ativos")
    public ResponseEntity<List<ServiceProviderResponse>> getActiveProviders() {
        List<ServiceProviderResponse> providers = serviceProviderService.getAllActiveProviders();
        return ResponseEntity.ok(providers);
    }

    /**
     * Lista fornecedores para administradores com filtro por status - endpoint raiz
     * 
     * @param status filtro por status: "active", "inactive" ou "all" (padrão:
     *               "all")
     * @return lista de fornecedores baseada no filtro de status
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar fornecedores com filtro (Admin)", description = "Retorna fornecedores filtrados por status. Valores: 'active', 'inactive' ou 'all'")
    public ResponseEntity<?> getAllProvidersWithFilter(
            @Parameter(description = "Filtro por status: active, inactive ou all") @RequestParam(defaultValue = "all") String status) {

        try {
            List<ServiceProviderResponse> providers;

            switch (status.toLowerCase()) {
                case "active":
                    providers = serviceProviderService.getAllActiveProviders();
                    break;
                case "inactive":
                    providers = serviceProviderService.getProvidersByStatus(false);
                    break;
                case "all":
                    providers = serviceProviderService.getAllProviders();
                    break;
                default:
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Status inválido. Use: 'active', 'inactive' ou 'all'");
                    return ResponseEntity.badRequest().body(error);
            }

            return ResponseEntity.ok(providers);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Busca fornecedor por ID
     * 
     * @param id ID do fornecedor
     * @return dados do fornecedor
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por ID", description = "Retorna dados completos de um fornecedor específico")
    public ResponseEntity<?> getProviderById(@Parameter(description = "ID do fornecedor") @PathVariable UUID id) {
        Optional<ServiceProviderResponse> provider = serviceProviderService.getProviderById(id);

        if (provider.isPresent()) {
            return ResponseEntity.ok(provider.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fornecedor não encontrado");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca fornecedores por nome (público)
     * 
     * @param name parte do nome para buscar
     * @return lista de fornecedores que correspondem ao nome
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar fornecedores por nome", description = "Busca fornecedores ativos que contenham o nome especificado")
    public ResponseEntity<List<ServiceProviderResponse>> searchProviders(
            @Parameter(description = "Nome ou parte do nome") @RequestParam String name) {
        List<ServiceProviderResponse> providers = serviceProviderService.searchActiveProvidersByName(name);
        return ResponseEntity.ok(providers);
    }

    /**
     * Busca fornecedores dentro de uma área geográfica (para mapa)
     * 
     * @param minLat latitude mínima
     * @param maxLat latitude máxima
     * @param minLng longitude mínima
     * @param maxLng longitude máxima
     * @return lista de fornecedores na área especificada
     */
    @GetMapping("/location")
    @Operation(summary = "Buscar fornecedores por localização", description = "Retorna fornecedores dentro de uma área geográfica específica (bounding box)")
    public ResponseEntity<?> getProvidersByLocation(
            @Parameter(description = "Latitude mínima") @RequestParam Double minLat,
            @Parameter(description = "Latitude máxima") @RequestParam Double maxLat,
            @Parameter(description = "Longitude mínima") @RequestParam Double minLng,
            @Parameter(description = "Longitude máxima") @RequestParam Double maxLng) {
        try {
            List<ServiceProviderResponse> providers = serviceProviderService.getProvidersByLocation(
                    minLat, maxLat, minLng, maxLng);
            return ResponseEntity.ok(providers);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Obtém estatísticas dos fornecedores (apenas administradores)
     * 
     * @return estatísticas dos fornecedores
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Estatísticas de fornecedores (Admin)", description = "Retorna estatísticas sobre fornecedores cadastrados")
    public ResponseEntity<Map<String, Object>> getProviderStats() {
        ServiceProviderService.ProviderStats stats = serviceProviderService.getProviderStats();

        Map<String, Object> response = new HashMap<>();
        response.put("total", stats.getTotal());
        response.put("active", stats.getActive());
        response.put("inactive", stats.getInactive());
        response.put("activePercentage",
                stats.getTotal() > 0 ? (double) stats.getActive() / stats.getTotal() * 100 : 0);

        return ResponseEntity.ok(response);
    }
}
