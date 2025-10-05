package fiap.backend.service;

import fiap.backend.domain.User;
import fiap.backend.dto.UserRegisterRequest;
import fiap.backend.dto.UserLoginRequest;
import fiap.backend.dto.AuthResponse;
import fiap.backend.dto.UserUpdateRequest;
import fiap.backend.dto.SubscriptionResponse;
import fiap.backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de autenticação e gerenciamento de usuários.
 * Responsável por registro, login, atualização de perfil e geração de tokens
 * JWT.
 */
@Service
public class AuthServiceImpl {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Registra um novo usuário no sistema
     * 
     * @param request dados do usuário para registro
     * @throws RuntimeException se o email já estiver cadastrado
     */
    public void register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }
        User user = new User();
        user.setFirstName(request.getName()); // Supondo que o DTO tem apenas name
        user.setLastName(""); // Ajuste conforme seu fluxo
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRolesByNames(Collections.singleton("USER"));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Atualiza os dados de um usuário específico (operação administrativa)
     * 
     * @param id      ID do usuário a ser atualizado
     * @param request novos dados do usuário
     * @return usuário atualizado
     * @throws RuntimeException se o usuário não for encontrado
     */
    public User updateUser(java.util.UUID id, UserRegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setFirstName(request.getName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Atualiza o perfil do usuário logado
     * 
     * @param username email do usuário logado
     * @param request  dados a serem atualizados
     * @return usuário atualizado
     * @throws RuntimeException se o usuário não for encontrado
     */
    public User updateMyProfile(String username, UserUpdateRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        // Adapte para os novos campos se necessário
        if (request.getTelefone() != null)
            user.setPhoneE164(request.getTelefone());
        if (request.getEndereco() != null)
            user.setAddressStreet(request.getEndereco());
        if (request.getProfilePictureUrl() != null)
            user.setProfilePhotoUrl(request.getProfilePictureUrl());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Busca um usuário pelo ID
     * 
     * @param id ID do usuário
     * @return dados do usuário
     * @throws RuntimeException se o usuário não for encontrado
     */
    public User getUser(java.util.UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Remove um usuário do sistema
     * 
     * @param id ID do usuário a ser removido
     */
    public void deleteUser(java.util.UUID id) {
        userRepository.deleteById(id);
    }

    /**
     * Autentica um usuário e gera token JWT com informações do plano ativo
     * 
     * @param request credenciais de login
     * @return response contendo o token JWT
     * @throws RuntimeException se as credenciais forem inválidas
     */
    public AuthResponse login(UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        // Buscar plano ativo do usuário para incluir no token
        List<SubscriptionResponse> userSubscriptions = subscriptionService.getSubscriptionsByUserId(user.getId());
        String activePlanName = null;
        String activePlanId = null;

        // Procurar por assinatura aprovada (ativa) - apenas uma é permitida
        for (SubscriptionResponse subscription : userSubscriptions) {
            if ("APPROVED".equals(subscription.getStatus().toString())) {
                activePlanName = subscription.getPlanName();
                activePlanId = subscription.getPlanId().toString();
                break; // Como só pode ter 1 plano ativo, pode parar na primeira
            }
        }

        // Use uma chave de 256 bits (32+ bytes) para HS256 - substitua por variável de
        // ambiente em produção
        String jwtSecret = "minha-super-chave-secreta-jwt-de-32-caracteres-segura-para-desenvolvimento-local";
        long expirationMs = 86400000; // 1 dia

        Key key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        // Gera token JWT com claims personalizadas incluindo plano ativo
        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("roles", user.getRoleNames())
                .claim("activePlanName", activePlanName) // Inclui nome do plano ativo
                .claim("activePlanId", activePlanId) // Inclui ID do plano ativo
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new AuthResponse(token);
    }

    /**
     * Promove um usuário para administrador
     * 
     * @param id ID do usuário a ser promovido
     * @throws RuntimeException se o usuário não for encontrado
     */
    public void promoteToAdmin(java.util.UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setRolesByNames(Collections.singleton("ADMIN"));
        userRepository.save(user);
    }

    /**
     * Lista todos os usuários do sistema
     * 
     * @return lista de todos os usuários
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
