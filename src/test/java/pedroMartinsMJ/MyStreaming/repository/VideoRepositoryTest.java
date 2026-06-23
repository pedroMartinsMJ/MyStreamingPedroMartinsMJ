package pedroMartinsMJ.MyStreaming.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.UserRole;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("VideoRepository - Testes de Banco")
class VideoRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve salvar um vídeo e associá-lo a um usuário com sucesso")
    void shouldSaveVideoAndAssociateToUser() {
        // Arrange
        User user = User.builder()
                .username("uploader")
                .email("uploader@email.com")
                .passwordHash("senha")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        userRepository.save(user);

        Video video = Video.builder()
                .title("Inception")
                .description("Um filme sobre sonhos dentro de sonhos")
                .originalFilePath("/storage/videos/inception.mp4")
                .user(user)
                .status(VideoStatus.UPLOADED)
                .tags(Collections.singleton("Sci-Fi"))
                .build();

        // Act
        Video savedVideo = videoRepository.save(video);
        Optional<Video> foundVideo = videoRepository.findById(savedVideo.getId());

        // Assert
        assertThat(savedVideo.getId()).isNotNull();
        assertThat(foundVideo).isPresent();
        assertThat(foundVideo.get().getTitle()).isEqualTo("Inception");
        assertThat(foundVideo.get().getUser().getUsername()).isEqualTo("uploader");
        assertThat(foundVideo.get().getTags()).contains("Sci-Fi");
    }

    @Test
    @DisplayName("Deve buscar vídeos por parte do título (case insensitive)")
    void shouldFindVideosByTitleContaining() {
        // Arrange
        User user = User.builder()
                .username("movie_fan")
                .email("fan@email.com")
                .passwordHash("senha")
                .role(UserRole.VIEWER)
                .active(true)
                .build();
        userRepository.save(user);

        Video v1 = Video.builder()
                .title("The Dark Knight")
                .originalFilePath("/storage/v1.mp4")
                .user(user)
                .status(VideoStatus.READY)
                .build();

        Video v2 = Video.builder()
                .title("Dark City")
                .originalFilePath("/storage/v2.mp4")
                .user(user)
                .status(VideoStatus.READY)
                .build();

        videoRepository.save(v1);
        videoRepository.save(v2);

        // Act
        List<Video> darkMovies = videoRepository.findByTitleContainingIgnoreCase("dark");

        // Assert
        assertThat(darkMovies).hasSize(2);
        assertThat(darkMovies).extracting(Video::getTitle)
                .containsExactlyInAnyOrder("The Dark Knight", "Dark City");
    }
}
