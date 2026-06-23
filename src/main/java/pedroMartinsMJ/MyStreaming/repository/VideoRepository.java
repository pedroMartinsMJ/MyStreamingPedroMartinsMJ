package pedroMartinsMJ.MyStreaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findByTitleContainingIgnoreCase(String title);
    List<Video> findByGenre(String genre);
    List<Video> findByUserId(UUID userId);
    List<Video> findByStatus(VideoStatus status);
    Optional<Video> findByOriginalFilePath(String filePath);
}
