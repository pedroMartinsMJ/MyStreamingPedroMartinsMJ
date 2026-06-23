package pedroMartinsMJ.MyStreaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pedroMartinsMJ.MyStreaming.model.EncodingJob;
import pedroMartinsMJ.MyStreaming.model.EncodingStatus;
import pedroMartinsMJ.MyStreaming.model.EncodingType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncodingJobDTO {
    private UUID id;
    private UUID videoId;
    private String videoTitle;
    private EncodingType encodingType;
    private String targetCodec;
    private Integer targetBitrate;
    private Integer targetWidth;
    private Integer targetHeight;
    private EncodingStatus status;
    private Double progressPercentage;
    private Long currentFrame;
    private Long totalFrames;
    private String eta;
    private String errorMessage;
    private Integer retryCount;
    private Integer maxRetries;
    private Double encodingSpeed;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static EncodingJobDTO from(EncodingJob job) {
        return EncodingJobDTO.builder()
                .id(job.getId())
                .videoId(job.getVideo() != null ? job.getVideo().getId() : null)
                .videoTitle(job.getVideo() != null ? job.getVideo().getTitle() : null)
                .encodingType(job.getEncodingType())
                .targetCodec(job.getTargetCodec())
                .targetBitrate(job.getTargetBitrate())
                .targetWidth(job.getTargetWidth())
                .targetHeight(job.getTargetHeight())
                .status(job.getStatus())
                .progressPercentage(job.getProgressPercentage())
                .currentFrame(job.getCurrentFrame())
                .totalFrames(job.getTotalFrames())
                .eta(job.getEta())
                .errorMessage(job.getErrorMessage())
                .retryCount(job.getRetryCount())
                .maxRetries(job.getMaxRetries())
                .encodingSpeed(job.getEncodingSpeed())
                .priority(job.getPriority())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
