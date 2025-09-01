package fiap.backend.controller;

import fiap.backend.dto.UserRegisterRequest;
import fiap.backend.dto.UserLoginRequest;
import fiap.backend.dto.AuthResponse;
import fiap.backend.service.AuthServiceImpl;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "APIs para registro, login e gerenciamento de usuários")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    @Operation(summary = "Verificar saúde do serviço", description = "Endpoint para verificar se o serviço de autenticação está funcionando")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serviço funcionando corretamente")
    })
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Auth service is running"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Obter perfil do usuário", description = "Retorna informações do perfil do usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(Map.of(
                "email", email,
                "message", "You are authenticated!",
                "timestamp", System.currentTimeMillis()));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "409", description = "Email já existe no sistema")
    })
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica o usuário e retorna um token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", 
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno: " + ex.getMessage()));
        }
    }

}
