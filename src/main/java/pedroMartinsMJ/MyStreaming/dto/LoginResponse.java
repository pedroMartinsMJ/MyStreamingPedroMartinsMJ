package pedroMartinsMJ.MyStreaming.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pedroMartinsMJ.MyStreaming.model.UserRole;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private UUID userId;
    private String username;
    private String displayName;
    private UserRole role;
}
