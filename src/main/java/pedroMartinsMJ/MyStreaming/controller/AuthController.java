package pedroMartinsMJ.MyStreaming.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pedroMartinsMJ.MyStreaming.dto.*;
import pedroMartinsMJ.MyStreaming.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login realizado com sucesso"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO dto = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Tokens JWT são stateless — o cliente simplesmente descarta o token.
        return ResponseEntity.ok(ApiResponse.ok(null, "Logout realizado com sucesso"));
    }
}
