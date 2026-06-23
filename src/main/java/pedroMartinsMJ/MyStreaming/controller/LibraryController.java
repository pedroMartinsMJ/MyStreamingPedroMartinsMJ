package pedroMartinsMJ.MyStreaming.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pedroMartinsMJ.MyStreaming.dto.ApiResponse;
import pedroMartinsMJ.MyStreaming.dto.VideoDTO;
import pedroMartinsMJ.MyStreaming.service.LibraryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> getUserLibrary(@PathVariable UUID userId) {
        List<VideoDTO> videos = libraryService.getUserLibrary(userId).stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> search(@RequestParam String q) {
        List<VideoDTO> videos = libraryService.search(q).stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> getByGenre(@PathVariable String genre) {
        List<VideoDTO> videos = libraryService.getByGenre(genre).stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }

    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> getReadyVideos() {
        List<VideoDTO> videos = libraryService.getReadyVideos().stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> scanDirectory(
            @RequestParam String dirPath,
            @RequestParam UUID userId) {
        List<VideoDTO> videos = libraryService.scanDirectory(dirPath, userId).stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(videos,
                "Scan concluído: " + videos.size() + " vídeos importados"));
    }
}
