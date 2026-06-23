package pedroMartinsMJ.MyStreaming.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "encoded_videos", indexes = {
    @Index(name = "idx_encoded_video_bitrate", columnList = "bitrate"),
    @Index(name = "idx_encoded_video_codec", columnList = "codec"),
    @Index(name = "idx_encoded_video_video", columnList = "video_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncodedVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    private String codec;

    @Column(name = "audio_codec")
    private String audioCodec;

    private Integer bitrate;

    @Column(name = "audio_bitrate")
    private Integer audioBitrate;

    private Integer width;
    private Integer height;

    @Column(name = "frame_rate")
    private Double frameRate;

    private String format;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "hls_base_path")
    private String hlsBasePath;

    @Column(name = "segment_duration_seconds")
    private Integer segmentDurationSeconds;

    @Column(name = "total_segments")
    private Long totalSegments;

    @Enumerated(EnumType.STRING)
    @Column(name = "encoding_status")
    private EncodingStatus encodingStatus;

    @Column(name = "encoding_time_seconds")
    private Long encodingTimeSeconds;

    @Column(name = "compression_ratio")
    private Double compressionRatio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
