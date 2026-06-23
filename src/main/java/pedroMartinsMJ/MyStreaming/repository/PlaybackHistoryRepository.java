package pedroMartinsMJ.MyStreaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pedroMartinsMJ.MyStreaming.model.PlaybackHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, UUID> {
    List<PlaybackHistory> findByUserIdOrderByPlayedAtDesc(UUID userId);
    Optional<PlaybackHistory> findByUserIdAndVideoId(UUID userId, UUID videoId);
    void deleteByUserIdAndVideoId(UUID userId, UUID videoId);
}
