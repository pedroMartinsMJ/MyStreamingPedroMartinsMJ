package pedroMartinsMJ.MyStreaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pedroMartinsMJ.MyStreaming.model.EncodingJob;
import pedroMartinsMJ.MyStreaming.model.EncodingStatus;
import java.util.List;
import java.util.UUID;

@Repository
public interface EncodingJobRepository extends JpaRepository<EncodingJob, UUID> {
    List<EncodingJob> findByStatus(EncodingStatus status);
    List<EncodingJob> findByVideoId(UUID videoId);
    List<EncodingJob> findByStatusOrderByCreatedAtAsc(EncodingStatus status);
    List<EncodingJob> findByStatusAndPriorityOrderByPriorityDesc(EncodingStatus status, int priority);
}
