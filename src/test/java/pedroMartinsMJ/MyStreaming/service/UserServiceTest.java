package pedroMartinsMJ.MyStreaming.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .displayName("Test User")
                .role(UserRole.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== LOGIN ====================

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Deve autenticar usuário válido com sucesso")
        void shouldLoginWithValidCredentials() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(jwtUtil.generateToken(eq(userId), eq("testuser"), eq("VIEWER"))).thenReturn("fake-jwt-token");

            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password123");
            LoginResponse response = userService.login(request);

            assertNotNull(response);
            assertEquals("Bearer", response.getTokenType());
            assertEquals(userId, response.getUserId());
            assertEquals("testuser", response.getUsername());
            assertEquals(UserRole.VIEWER, response.getRole());
            verify(userRepository).save(testUser); // lastLogin updated
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException para usuário inexistente")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest();
            request.setUsername("unknown");
            request.setPassword("password123");

            assertThrows(UserNotFoundException.class, () -> userService.login(request));
            verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve lançar IllegalStateException para usuário inativo")
        void shouldThrowWhenUserInactive() {
            testUser.setActive(false);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            assertThrows(IllegalStateException.class, () -> userService.login(request));
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para senha incorreta")
        void shouldThrowWhenPasswordIncorrect() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        }
    }

    // ==================== CREATE USER ====================

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Deve criar usuário com dados válidos")
        void shouldCreateUserWithValidData() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("secure123");
            request.setDisplayName("New User");

            UserDTO result = userService.createUser(request);

            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve usar role VIEWER quando não especificada")
        void shouldDefaultToViewerRole() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                assertEquals(UserRole.VIEWER, saved.getRole());
                return saved;
            });

            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("secure123");

            userService.createUser(request);
        }

        @Test
        @DisplayName("Deve usar username como displayName quando não especificado")
        void shouldUseUsernameAsDisplayNameWhenNotProvided() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                assertEquals("newuser", saved.getDisplayName());
                return saved;
            });

            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("secure123");
            request.setDisplayName(null); // explicit null

            userService.createUser(request);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para username duplicado")
        void shouldThrowWhenUsernameTaken() {
            when(userRepository.existsByUsername("taken")).thenReturn(true);

            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("taken");
            request.setEmail("t@example.com");
            request.setPassword("secure123");

            assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
        }
    }

    // ==================== GET USER ====================

    @Nested
    @DisplayName("getUserById() e getUserByUsername()")
    class GetUserTests {

        @Test
        @DisplayName("Deve retornar usuário por ID")
        void shouldGetUserById() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            UserDTO result = userService.getUserById(userId);

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado por ID")
        void shouldThrowWhenGetUserByIdNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        }

        @Test
        @DisplayName("Deve retornar usuário por username")
        void shouldGetUserByUsername() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            UserDTO result = userService.getUserByUsername("testuser");

            assertNotNull(result);
            assertEquals(userId, result.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado por username")
        void shouldThrowWhenGetUserByUsernameNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("unknown"));
        }
    }

    // ==================== CHANGE PASSWORD ====================

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Deve alterar senha com sucesso")
        void shouldChangePasswordSuccessfully() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            userService.changePassword(userId, "password123", "newpassword456");

            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Deve lançar exceção para senha atual incorreta")
        void shouldThrowWhenOldPasswordIncorrect() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            assertThrows(IllegalArgumentException.class, () ->
                    userService.changePassword(userId, "wrongpassword", "newpassword"));
        }

        @Test
        @DisplayName("Deve lançar exceção para usuário inexistente")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () ->
                    userService.changePassword(userId, "password123", "newpassword"));
        }
    }

    // ==================== DEACTIVATE USER ====================

    @Nested
    @DisplayName("deactivateUser()")
    class DeactivateUserTests {

        @Test
        @DisplayName("Deve desativar usuário com sucesso")
        void shouldDeactivateUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            userService.deactivateUser(userId);

            assertFalse(testUser.isActive());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Deve lançar exceção para usuário inexistente")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(userId));
        }
    }

    // ==================== CREATE DEFAULT ADMIN ====================

    @Nested
    @DisplayName("createDefaultAdminIfNeeded()")
    class CreateDefaultAdminTests {

        @Test
        @DisplayName("Deve criar admin quando não existe")
        void shouldCreateAdminWhenNotExists() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

            userService.createDefaultAdminIfNeeded();

            verify(userRepository).save(argThat(u ->
                    "admin".equals(u.getUsername()) &&
                    UserRole.ADMIN.equals(u.getRole()) &&
                    u.isActive()
            ));
        }

        @Test
        @DisplayName("Não deve criar admin quando já existe")
        void shouldNotCreateAdminWhenExists() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

            userService.createDefaultAdminIfNeeded();

            verify(userRepository, never()).save(any());
        }
    }

    // ==================== GET ALL USERS ====================

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsersTests {

        @Test
        @DisplayName("Deve retornar todos os usuários como DTOs")
        void shouldReturnAllUsersAsDTOs() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<UserDTO> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("testuser", result.get(0).getUsername());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserDTO> result = userService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}