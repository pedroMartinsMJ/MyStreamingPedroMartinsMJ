package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pedroMartinsMJ.MyStreaming.dto.CreateUserRequest;
import pedroMartinsMJ.MyStreaming.dto.LoginRequest;
import pedroMartinsMJ.MyStreaming.dto.LoginResponse;
import pedroMartinsMJ.MyStreaming.dto.UserDTO;
import pedroMartinsMJ.MyStreaming.exception.UserNotFoundException;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.UserRole;
import pedroMartinsMJ.MyStreaming.repository.UserRepository;
import pedroMartinsMJ.MyStreaming.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException(request.getUsername()));

        if (!user.isActive()) {
            throw new IllegalStateException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        log.info("Usuário {} autenticado com sucesso", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username já está em uso: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
                .role(request.getRole() != null ? request.getRole() : UserRole.VIEWER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Novo usuário criado: {}", saved.getUsername());
        return UserDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(UserDTO::from)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserDTO::from)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::from)
                .toList();
    }

    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Senha alterada para usuário: {}", user.getUsername());
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        user.setActive(false);
        userRepository.save(user);
        log.info("Usuário desativado: {}", user.getUsername());
    }

    @Transactional
    public void createDefaultAdminIfNeeded() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@mystreaming.local")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .displayName("Administrador")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.warn("Usuário admin padrão criado. Troque a senha em produção!");
        }
    }
}
