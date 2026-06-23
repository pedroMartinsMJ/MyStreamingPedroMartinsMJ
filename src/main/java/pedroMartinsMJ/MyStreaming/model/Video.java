package pedroMartinsMJ.MyStreaming.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "videos", indexes = {
    @Index(name = "idx_video_status", columnList = "status"),
    @Index(name = "idx_video_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 65535)
    private String description;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_file_path", nullable = false)
    private String originalFilePath;

    @Column(name = "original_format")
    private String originalFormat;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "original_width")
    private Integer originalWidth;

    @Column(name = "original_height")
    private Integer originalHeight;

    @Column(name = "original_codec")
    private String originalCodec;

    @Column(name = "original_bitrate")
    private Integer originalBitrate;

    @Column(name = "frame_rate")
    private Double frameRate;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    private String genre;
    private String director;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "imdb_rating")
    private Double imdbRating;

    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(name = "error_message", length = 65535)
    private String errorMessage;

    @Column(name = "video_quality_score")
    private Double videoQualityScore;

    @Column(name = "video_quality_analysis", length = 65535)
    private String videoQualityAnalysis;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "times_watched")
    private Long timesWatched;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "video_tags", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "tag")
    private Set<String> tags;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EncodedVideo> encodedVersions;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EncodingJob> encodingJobs;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtitle> subtitles;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        if (this.timesWatched == null) {
            this.timesWatched = 0L;
        }
    }
}
