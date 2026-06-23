package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pedroMartinsMJ.MyStreaming.exception.VideoNotFoundException;
import pedroMartinsMJ.MyStreaming.model.EncodedVideo;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;
import pedroMartinsMJ.MyStreaming.repository.EncodedVideoRepository;
import pedroMartinsMJ.MyStreaming.repository.VideoRepository;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final VideoRepository videoRepository;
    private final EncodedVideoRepository encodedVideoRepository;
    private final FileSystemService fileSystemService;

    /**
     * Gera a master playlist HLS com todas as variantes disponíveis.
     */
    public String generateMasterPlaylist(UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        if (video.getStatus() != VideoStatus.READY) {
            throw new IllegalStateException("Vídeo ainda não está pronto para streaming. Status: " + video.getStatus());
        }

        List<EncodedVideo> versions = encodedVideoRepository.findByVideoId(videoId);

        StringBuilder playlist = new StringBuilder();
        playlist.append("#EXTM3U\n");
        playlist.append("#EXT-X-VERSION:3\n\n");

        for (EncodedVideo ev : versions) {
            int bandwidth = ev.getBitrate() * 1000;
            playlist.append("#EXT-X-STREAM-INF:")
                    .append("BANDWIDTH=").append(bandwidth).append(",")
                    .append("RESOLUTION=").append(ev.getWidth()).append("x").append(ev.getHeight()).append(",")
                    .append("CODECS=\"avc1.42E01E,mp4a.40.2\"\n");
            playlist.append("/api/stream/").append(videoId)
                    .append("/variant/").append(ev.getBitrate()).append("k/playlist.m3u8\n\n");
        }

        // Fallback: stream direto se não houver versões HLS
        if (versions.isEmpty()) {
            playlist.append("#EXT-X-STREAM-INF:BANDWIDTH=8000000\n");
            playlist.append("/api/stream/").append(videoId).append("/direct\n");
        }

        return playlist.toString();
    }

    /**
     * Retorna a variant playlist de um bitrate específico.
     */
    public String getVariantPlaylist(UUID videoId, String bitrate) {
        EncodedVideo ev = encodedVideoRepository.findByVideoIdAndBitrate(videoId,
                        Integer.parseInt(bitrate.replace("k", "")))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variante não disponível: " + bitrate + " para vídeo " + videoId));

        String playlistPath = Paths.get(ev.getHlsBasePath(), "playlist.m3u8").toString();
        File playlistFile = new File(playlistPath);

        if (!playlistFile.exists()) {
            throw new IllegalStateException("Playlist HLS não encontrada: " + playlistPath);
        }

        try {
            return Files.readString(playlistFile.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler playlist: " + playlistPath, e);
        }
    }

    /**
     * Retorna um segmento .ts HLS.
     */
    public Resource getSegment(UUID videoId, String bitrate, String segmentName) {
        EncodedVideo ev = encodedVideoRepository.findByVideoIdAndBitrate(videoId,
                        Integer.parseInt(bitrate.replace("k", "")))
                .orElseThrow(() -> new IllegalArgumentException("Variante não encontrada: " + bitrate));

        Path segmentPath = Paths.get(ev.getHlsBasePath(), segmentName);
        if (!Files.exists(segmentPath)) {
            throw new IllegalArgumentException("Segmento não encontrado: " + segmentName);
        }

        return new FileSystemResource(segmentPath);
    }

    /**
     * Streaming direto do arquivo original com suporte a Range Requests.
     */
    public ResponseEntity<byte[]> streamVideoDirectly(UUID videoId, String rangeHeader) throws IOException {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        File file = fileSystemService.getFile(video.getOriginalFilePath());
        if (!file.exists()) {
            throw new IllegalStateException("Arquivo não encontrado: " + video.getOriginalFilePath());
        }

        long fileSize = file.length();
        String mimeType = guessMimeType(video.getOriginalFormat());

        if (rangeHeader == null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, mimeType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                    .body(Files.readAllBytes(file.toPath()));
        }

        // Processar Range Request
        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
        if (ranges.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(fileSize);
        long end   = range.getRangeEnd(fileSize);
        long length = end - start + 1;

        byte[] data = new byte[(int) length];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(start);
            raf.readFully(data);
        }

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(length))
                .body(data);
    }

    private String guessMimeType(String format) {
        if (format == null) return "video/mp4";
        return switch (format.toLowerCase()) {
            case "mp4", "m4v" -> "video/mp4";
            case "mkv"        -> "video/x-matroska";
            case "avi"        -> "video/x-msvideo";
            case "webm"       -> "video/webm";
            case "mov"        -> "video/quicktime";
            default           -> "video/mp4";
        };
    }
}
