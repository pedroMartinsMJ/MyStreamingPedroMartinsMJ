package pedroMartinsMJ.MyStreaming.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "encoding_jobs", indexes = {
    @Index(name = "idx_encoding_job_status", columnList = "status"),
    @Index(name = "idx_encoding_job_video", columnList = "video_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncodingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Enumerated(EnumType.STRING)
    @Column(name = "encoding_type")
    private EncodingType encodingType;

    @Column(name = "target_codec")
    private String targetCodec;

    @Column(name = "target_bitrate")
    private Integer targetBitrate;

    @Column(name = "target_width")
    private Integer targetWidth;

    @Column(name = "target_height")
    private Integer targetHeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncodingStatus status;

    @Column(name = "progress_percentage")
    private Double progressPercentage;

    @Column(name = "current_frame")
    private Long currentFrame;

    @Column(name = "total_frames")
    private Long totalFrames;

    private String eta;

    @Column(name = "error_message", length = 65535)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "encoding_speed")
    private Double encodingSpeed;

    private Integer priority;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "log_content", length = 65535)
    private String logContent;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.maxRetries == null) {
            this.maxRetries = 3;
        }
        if (this.priority == null) {
            this.priority = 5;
        }
        if (this.progressPercentage == null) {
            this.progressPercentage = 0.0;
        }
    }
}
