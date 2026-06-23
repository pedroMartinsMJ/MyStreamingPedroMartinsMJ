package pedroMartinsMJ.MyStreaming.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pedroMartinsMJ.MyStreaming.dto.ApiResponse;
import pedroMartinsMJ.MyStreaming.dto.VideoDTO;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;
import pedroMartinsMJ.MyStreaming.service.EncodingService;
import pedroMartinsMJ.MyStreaming.service.UserService;
import pedroMartinsMJ.MyStreaming.service.VideoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final EncodingService encodingService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VideoDTO>>> listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Page<VideoDTO> result = videoService.getAllVideos(pageable).map(VideoDTO::from);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDTO>> getVideo(@PathVariable UUID id) {
        Video video = videoService.getVideoById(id);
        return ResponseEntity.ok(ApiResponse.ok(VideoDTO.from(video)));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<VideoDTO>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean startEncoding,
            @AuthenticationPrincipal UserDetails userDetails) {

        var user = userService.getUserByUsername(userDetails.getUsername());
        Video video = videoService.uploadVideo(file, title, description, user.getId());

        if (startEncoding) {
            encodingService.startAdaptiveEncoding(video.getId());
        }

        return ResponseEntity.accepted()
                .body(ApiResponse.ok(VideoDTO.from(video), "Vídeo enviado com sucesso"));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<VideoDTO>> importVideo(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "false") boolean startEncoding,
            @AuthenticationPrincipal UserDetails userDetails) {

        var user = userService.getUserByUsername(userDetails.getUsername());
        Video video = videoService.importFromPath(filePath, user.getId());

        if (startEncoding) {
            encodingService.startAdaptiveEncoding(video.getId());
        }

        return ResponseEntity.accepted()
                .body(ApiResponse.ok(VideoDTO.from(video), "Vídeo importado com sucesso"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDTO>> updateVideo(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) Integer releaseYear) {

        Video updated = videoService.updateVideo(id, title, description, genre, director, releaseYear);
        return ResponseEntity.ok(ApiResponse.ok(VideoDTO.from(updated), "Vídeo atualizado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable UUID id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Vídeo removido"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> searchVideos(@RequestParam String q) {
        List<VideoDTO> results = videoService.searchVideos(q).stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> getByStatus(@PathVariable VideoStatus status) {
        List<VideoDTO> results = videoService.getVideosByStatus(status).stream()
                .map(VideoDTO::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping("/{id}/encode")
    public ResponseEntity<ApiResponse<Void>> triggerEncoding(@PathVariable UUID id) {
        encodingService.startAdaptiveEncoding(id);
        return ResponseEntity.accepted()
                .body(ApiResponse.ok(null, "Encoding iniciado"));
    }
}
