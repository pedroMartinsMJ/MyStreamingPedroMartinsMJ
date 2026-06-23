package pedroMartinsMJ.MyStreaming.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pedroMartinsMJ.MyStreaming.service.StreamingService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    /**
     * Master HLS playlist — o player usa para escolher a qualidade automaticamente
     */
    @GetMapping(value = "/{videoId}/playlist.m3u8", produces = "application/vnd.apple.mpegurl")
    public ResponseEntity<String> getMasterPlaylist(@PathVariable UUID videoId) {
        String playlist = streamingService.generateMasterPlaylist(videoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(playlist);
    }

    /**
     * Variant playlist de um bitrate específico (ex: /api/stream/{id}/variant/5000k/playlist.m3u8)
     */
    @GetMapping(value = "/{videoId}/variant/{bitrate}/playlist.m3u8",
                produces = "application/vnd.apple.mpegurl")
    public ResponseEntity<String> getVariantPlaylist(
            @PathVariable UUID videoId,
            @PathVariable String bitrate) {
        String playlist = streamingService.getVariantPlaylist(videoId, bitrate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(playlist);
    }

    /**
     * Segmento .ts de um bitrate específico
     */
    @GetMapping("/{videoId}/variant/{bitrate}/{segment:.+\\.ts}")
    public ResponseEntity<Resource> getSegment(
            @PathVariable UUID videoId,
            @PathVariable String bitrate,
            @PathVariable String segment) {
        Resource resource = streamingService.getSegment(videoId, bitrate, segment);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/MP2T"))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }

    /**
     * Streaming direto do arquivo original com suporte a Range Requests (seek)
     */
    @GetMapping("/{videoId}/direct")
    public ResponseEntity<byte[]> streamDirect(
            @PathVariable UUID videoId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader)
            throws IOException {
        return streamingService.streamVideoDirectly(videoId, rangeHeader);
    }
}
