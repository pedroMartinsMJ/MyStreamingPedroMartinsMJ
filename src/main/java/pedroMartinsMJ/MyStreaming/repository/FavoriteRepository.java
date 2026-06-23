package pedroMartinsMJ.MyStreaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pedroMartinsMJ.MyStreaming.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);
    Optional<Favorite> findByUserIdAndVideoId(UUID userId, UUID videoId);
    boolean existsByUserIdAndVideoId(UUID userId, UUID videoId);
}
