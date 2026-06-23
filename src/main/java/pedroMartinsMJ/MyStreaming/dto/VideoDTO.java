package pedroMartinsMJ.MyStreaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private UUID id;
    private String title;
    private String description;
    private String posterPath;
    private String thumbnailPath;
    private UUID userId;
    private String originalFormat;
    private Long fileSize;
    private Integer originalWidth;
    private Integer originalHeight;
    private String originalCodec;
    private Integer originalBitrate;
    private Double frameRate;
    private Long durationSeconds;
    private String genre;
    private String director;
    private Integer releaseYear;
    private Double imdbRating;
    private String language;
    private VideoStatus status;
    private String errorMessage;
    private Long timesWatched;
    private Set<String> tags;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastAccessedAt;

    public static VideoDTO from(Video video) {
        return VideoDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .posterPath(video.getPosterPath())
                .thumbnailPath(video.getThumbnailPath())
                .userId(video.getUser() != null ? video.getUser().getId() : null)
                .originalFormat(video.getOriginalFormat())
                .fileSize(video.getFileSize())
                .originalWidth(video.getOriginalWidth())
                .originalHeight(video.getOriginalHeight())
                .originalCodec(video.getOriginalCodec())
                .originalBitrate(video.getOriginalBitrate())
                .frameRate(video.getFrameRate())
                .durationSeconds(video.getDurationSeconds())
                .genre(video.getGenre())
                .director(video.getDirector())
                .releaseYear(video.getReleaseYear())
                .imdbRating(video.getImdbRating())
                .language(video.getLanguage())
                .status(video.getStatus())
                .errorMessage(video.getErrorMessage())
                .timesWatched(video.getTimesWatched())
                .tags(video.getTags())
                .uploadedAt(video.getUploadedAt())
                .lastAccessedAt(video.getLastAccessedAt())
                .build();
    }
}
