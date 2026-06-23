package pedroMartinsMJ.MyStreaming.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "playback_histories", indexes = {
    @Index(name = "idx_playback_user", columnList = "user_id"),
    @Index(name = "idx_playback_video", columnList = "video_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaybackHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "current_position_seconds")
    private Long currentPositionSeconds;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "device_info")
    private String deviceInfo;

    private String resolution;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "watch_time_seconds")
    private Long watchTimeSeconds;

    private boolean completed;

    @Column(name = "avg_bitrate")
    private Double avgBitrate;

    @PrePersist
    protected void onCreate() {
        this.playedAt = LocalDateTime.now();
        if (this.currentPositionSeconds == null) {
            this.currentPositionSeconds = 0L;
        }
        if (this.watchTimeSeconds == null) {
            this.watchTimeSeconds = 0L;
        }
    }
}
