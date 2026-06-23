package pedroMartinsMJ.MyStreaming.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.UserRole;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepository - Testes de Banco")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve salvar um usuário com sucesso e buscá-lo por username")
    void shouldSaveAndFindUserByUsername() {
        // Arrange
        User user = User.builder()
                .username("testeUser")
                .email("teste@email.com")
                .passwordHash("hash_de_senha_segura")
                .role(UserRole.VIEWER)
                .displayName("Teste User")
                .active(true)
                .build();

        // Act
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByUsername("testeUser");

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("teste@email.com");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.VIEWER);
        assertThat(foundUser.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar se um usuário existe por username")
    void shouldCheckIfExistsByUsername() {
        // Arrange
        User user = User.builder()
                .username("existsUser")
                .email("exists@email.com")
                .passwordHash("hash_de_senha")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername("existsUser");
        boolean notExists = userRepository.existsByUsername("randomUser");

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
