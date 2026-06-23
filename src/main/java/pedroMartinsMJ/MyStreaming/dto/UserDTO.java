package pedroMartinsMJ.MyStreaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private UserRole role;
    private String preferredResolution;
    private String preferredLanguage;
    private Boolean enableSubtitles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .preferredResolution(user.getPreferredResolution())
                .preferredLanguage(user.getPreferredLanguage())
                .enableSubtitles(user.getEnableSubtitles())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .active(user.isActive())
                .build();
    }
}
