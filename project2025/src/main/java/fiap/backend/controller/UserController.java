package fiap.backend.controller;

import fiap.backend.dto.UserRegisterRequest;
import fiap.backend.dto.UserUpdateRequest;
import fiap.backend.service.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Gestão de Usuários", description = "APIs para gerenciar perfis e dados dos usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthServiceImpl authService;

    public UserController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza dados de um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<?> updateUser(@PathVariable java.util.UUID id, @RequestBody UserRegisterRequest request) {
        var user = authService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter usuário", description = "Retorna dados de um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<?> getUser(@PathVariable java.util.UUID id) {
        var user = authService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário", description = "Remove um usuário do sistema (apenas administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    })
    public ResponseEntity<?> deleteUser(@PathVariable java.util.UUID id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/promote")
    @Operation(summary = "Promover a administrador", description = "Promove um usuário a administrador (apenas administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário promovido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    })
    public ResponseEntity<?> promoteToAdmin(@PathVariable java.util.UUID id) {
        authService.promoteToAdmin(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    @Operation(summary = "Atualizar meu perfil", description = "Permite ao usuário autenticado atualizar seu próprio perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request) {
        var updatedUser = authService.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedUser);
    }
}
