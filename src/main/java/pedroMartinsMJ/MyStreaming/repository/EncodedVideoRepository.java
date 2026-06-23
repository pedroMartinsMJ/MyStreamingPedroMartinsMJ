package pedroMartinsMJ.MyStreaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pedroMartinsMJ.MyStreaming.model.EncodedVideo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EncodedVideoRepository extends JpaRepository<EncodedVideo, UUID> {
    List<EncodedVideo> findByVideoId(UUID videoId);
    Optional<EncodedVideo> findByVideoIdAndBitrate(UUID videoId, Integer bitrate);
    List<EncodedVideo> findByCodec(String codec);
}
