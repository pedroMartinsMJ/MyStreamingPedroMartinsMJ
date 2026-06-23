package pedroMartinsMJ.MyStreaming.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ffmpeg_path")
    private String ffmpegPath;

    @Column(name = "ffprobe_path")
    private String ffprobePath;

    @Column(name = "max_concurrent_encoding_jobs")
    private Integer maxConcurrentEncodingJobs;

    @Column(name = "default_encoding_codec")
    private String defaultEncodingCodec;

    @Column(name = "video_storage_path")
    private String videoStoragePath;

    @Column(name = "encoded_storage_path")
    private String encodedStoragePath;

    @Column(name = "temp_working_path")
    private String tempWorkingPath;

    @Column(name = "max_storage_gb")
    private Long maxStorageGB;

    @Column(name = "streaming_protocol")
    private String streamingProtocol;

    @Column(name = "hls_segment_duration")
    private Integer hlsSegmentDuration;

    @Column(name = "available_bitrates")
    private String availableBitrates;

    @Column(name = "jwt_secret")
    private String jwtSecret;

    @Column(name = "jwt_expiration_hours")
    private Integer jwtExpirationHours;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @Column(name = "poster_width")
    private Integer posterWidth;

    @Column(name = "poster_height")
    private Integer posterHeight;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
