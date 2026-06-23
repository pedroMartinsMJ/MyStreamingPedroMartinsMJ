package pedroMartinsMJ.MyStreaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pedroMartinsMJ.MyStreaming.model.PlaybackHistory;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackHistoryDTO {
    private UUID id;
    private UUID userId;
    private UUID videoId;
    private String videoTitle;
    private Long currentPositionSeconds;
    private Long durationSeconds;
    private String deviceInfo;
    private String resolution;
    private LocalDateTime playedAt;
    private LocalDateTime pausedAt;
    private Long watchTimeSeconds;
    private boolean completed;
    private Double avgBitrate;

    public static PlaybackHistoryDTO from(PlaybackHistory h) {
        return PlaybackHistoryDTO.builder()
                .id(h.getId())
                .userId(h.getUser() != null ? h.getUser().getId() : null)
                .videoId(h.getVideo() != null ? h.getVideo().getId() : null)
                .videoTitle(h.getVideo() != null ? h.getVideo().getTitle() : null)
                .currentPositionSeconds(h.getCurrentPositionSeconds())
                .durationSeconds(h.getDurationSeconds())
                .deviceInfo(h.getDeviceInfo())
                .resolution(h.getResolution())
                .playedAt(h.getPlayedAt())
                .pausedAt(h.getPausedAt())
                .watchTimeSeconds(h.getWatchTimeSeconds())
                .completed(h.isCompleted())
                .avgBitrate(h.getAvgBitrate())
                .build();
    }
}
