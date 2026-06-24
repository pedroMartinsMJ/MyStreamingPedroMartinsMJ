package pedroMartinsMJ.MyStreaming.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pedroMartinsMJ.MyStreaming.model.PlaybackHistory;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.repository.PlaybackHistoryRepository;
import pedroMartinsMJ.MyStreaming.repository.UserRepository;
import pedroMartinsMJ.MyStreaming.repository.VideoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaybackHistoryServiceTest {

    @Mock
    private PlaybackHistoryRepository playbackHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private PlaybackHistoryService playbackHistoryService;

    private UUID userId;
    private UUID videoId;
    private User testUser;
    private Video testVideo;
    private PlaybackHistory history;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        videoId = UUID.randomUUID();

        testUser = User.builder().id(userId).username("testuser").build();
        testVideo = Video.builder().id(videoId).title("Test Video").durationSeconds(3600L).build();

        history = PlaybackHistory.builder()
                .user(testUser)
                .video(testVideo)
                .currentPositionSeconds(120L)
                .playedAt(LocalDateTime.now())
                .completed(false)
                .build();
    }

    // ==================== RECORD PLAYBACK START ====================

    @Nested
    @DisplayName("recordPlaybackStart()")
    class RecordPlaybackStartTests {

        @Test
        @DisplayName("Deve criar novo registro quando não existe histórico")
        void shouldCreateNewRecordWhenNoHistoryExists() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(videoRepository.findById(videoId)).thenReturn(Optional.of(testVideo));
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());
            when(playbackHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            PlaybackHistory result = playbackHistoryService.recordPlaybackStart(userId, videoId, "Chrome/123");

            assertNotNull(result);
            assertEquals(testUser, result.getUser());
            assertEquals(testVideo, result.getVideo());
            verify(playbackHistoryRepository).save(any());
        }

        @Test
        @DisplayName("Deve reutilizar registro existente")
        void shouldReuseExistingRecord() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(videoRepository.findById(videoId)).thenReturn(Optional.of(testVideo));
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(history));
            when(playbackHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            PlaybackHistory result = playbackHistoryService.recordPlaybackStart(userId, videoId, "Chrome/123");

            assertNotNull(result);
            assertEquals("Chrome/123", result.getDeviceInfo());
            assertFalse(result.isCompleted());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    playbackHistoryService.recordPlaybackStart(userId, videoId, "Chrome/123"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando vídeo não encontrado")
        void shouldThrowWhenVideoNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    playbackHistoryService.recordPlaybackStart(userId, videoId, "Chrome/123"));
        }
    }

    // ==================== UPDATE PLAYBACK POSITION ====================

    @Nested
    @DisplayName("updatePlaybackPosition()")
    class UpdatePlaybackPositionTests {

        @Test
        @DisplayName("Deve atualizar posição de reprodução")
        void shouldUpdatePosition() {
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(history));
            when(playbackHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            playbackHistoryService.updatePlaybackPosition(userId, videoId, 300L);

            assertEquals(300L, history.getCurrentPositionSeconds());
            verify(playbackHistoryRepository).save(history);
        }

        @Test
        @DisplayName("Deve marcar como completado quando posição >= 90% da duração")
        void shouldMarkAsCompletedWhenOver90Percent() {
            // Duração = 100s, posição = 95s (95%)
            history.setDurationSeconds(100L);
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(history));
            when(playbackHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            playbackHistoryService.updatePlaybackPosition(userId, videoId, 95L);

            assertTrue(history.isCompleted());
        }

        @Test
        @DisplayName("Não deve marcar como completado quando posição < 90%")
        void shouldNotMarkAsCompletedWhenUnder90Percent() {
            history.setDurationSeconds(100L);
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(history));
            when(playbackHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            playbackHistoryService.updatePlaybackPosition(userId, videoId, 89L);

            assertFalse(history.isCompleted());
        }

        @Test
        @DisplayName("Não deve lançar exceção quando não existe histórico")
        void shouldNotThrowWhenNoHistoryExists() {
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());

            // Não deve lançar exceção — apenas ignora silenciosamente
            assertDoesNotThrow(() -> playbackHistoryService.updatePlaybackPosition(userId, videoId, 100L));
            verify(playbackHistoryRepository, never()).save(any());
        }
    }

    // ==================== GET LAST POSITION ====================

    @Nested
    @DisplayName("getLastPosition()")
    class GetLastPositionTests {

        @Test
        @DisplayName("Deve retornar última posição quando existe histórico")
        void shouldReturnLastPositionWhenHistoryExists() {
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.of(history));

            Optional<Long> result = playbackHistoryService.getLastPosition(userId, videoId);

            assertTrue(result.isPresent());
            assertEquals(120L, result.get());
        }

        @Test
        @DisplayName("Deve retornar Optional.empty quando não existe histórico")
        void shouldReturnEmptyWhenNoHistoryExists() {
            when(playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)).thenReturn(Optional.empty());

            Optional<Long> result = playbackHistoryService.getLastPosition(userId, videoId);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== GET USER HISTORY ====================

    @Nested
    @DisplayName("getUserHistory()")
    class GetUserHistoryTests {

        @Test
        @DisplayName("Deve retornar histórico do usuário ordenado por data")
        void shouldReturnUserHistoryOrderedByDate() {
            when(playbackHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId)).thenReturn(List.of(history));

            List<PlaybackHistory> result = playbackHistoryService.getUserHistory(userId);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há histórico")
        void shouldReturnEmptyListWhenNoHistory() {
            when(playbackHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId)).thenReturn(List.of());

            List<PlaybackHistory> result = playbackHistoryService.getUserHistory(userId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== CLEAR HISTORY ====================

    @Nested
    @DisplayName("clearHistory()")
    class ClearHistoryTests {

        @Test
        @DisplayName("Deve limpar histórico do usuário para um vídeo específico")
        void shouldClearHistoryForUserAndVideo() {
            playbackHistoryService.clearHistory(userId, videoId);

            verify(playbackHistoryRepository).deleteByUserIdAndVideoId(userId, videoId);
        }
    }
}