package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaybackHistoryService {

    private final PlaybackHistoryRepository playbackHistoryRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public PlaybackHistory recordPlaybackStart(UUID userId, UUID videoId, String deviceInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado: " + videoId));

        // Reutilizar registro existente ou criar novo
        PlaybackHistory history = playbackHistoryRepository
                .findByUserIdAndVideoId(userId, videoId)
                .orElse(PlaybackHistory.builder()
                        .user(user)
                        .video(video)
                        .currentPositionSeconds(0L)
                        .build());

        history.setPlayedAt(LocalDateTime.now());
        history.setDeviceInfo(deviceInfo);
        history.setCompleted(false);

        return playbackHistoryRepository.save(history);
    }

    @Transactional
    public void updatePlaybackPosition(UUID userId, UUID videoId, long positionSeconds) {
        playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .ifPresent(history -> {
                    history.setCurrentPositionSeconds(positionSeconds);
                    history.setPausedAt(LocalDateTime.now());

                    // Verificar se completou (>90% do vídeo)
                    if (history.getDurationSeconds() != null && history.getDurationSeconds() > 0) {
                        double progress = (double) positionSeconds / history.getDurationSeconds();
                        if (progress >= 0.9) {
                            history.setCompleted(true);
                        }
                    }

                    playbackHistoryRepository.save(history);
                });
    }

    @Transactional(readOnly = true)
    public Optional<Long> getLastPosition(UUID userId, UUID videoId) {
        return playbackHistoryRepository.findByUserIdAndVideoId(userId, videoId)
                .map(PlaybackHistory::getCurrentPositionSeconds);
    }

    @Transactional(readOnly = true)
    public List<PlaybackHistory> getUserHistory(UUID userId) {
        return playbackHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId);
    }

    @Transactional
    public void clearHistory(UUID userId, UUID videoId) {
        playbackHistoryRepository.deleteByUserIdAndVideoId(userId, videoId);
    }
}
