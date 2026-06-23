package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemService {

    private static final Set<String> MEDIA_EXTENSIONS = Set.of(
            "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "m4v",
            "jpg", "jpeg", "png", "gif", "webp"
    );

    @Value("${storage.video-path:./storage/videos}")
    private String videoStoragePath;

    @Value("${storage.encoded-path:./storage/encoded}")
    private String encodedStoragePath;

    @Value("${storage.thumbnail-path:./storage/thumbnails}")
    private String thumbnailStoragePath;

    @Value("${storage.temp-path:./storage/temp}")
    private String tempPath;

    public String getVideoStoragePath() {
        ensureDirectoryExists(videoStoragePath);
        return videoStoragePath;
    }

    public String getEncodedStoragePath() {
        ensureDirectoryExists(encodedStoragePath);
        return encodedStoragePath;
    }

    public String getThumbnailStoragePath() {
        ensureDirectoryExists(thumbnailStoragePath);
        return thumbnailStoragePath;
    }

    public String getTempPath() {
        ensureDirectoryExists(tempPath);
        return tempPath;
    }

    public File getFile(String filePath) {
        return new File(filePath);
    }

    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    public long getFileSize(String filePath) {
        return new File(filePath).length();
    }

    public long getAvailableDiskSpace() {
        return new File(videoStoragePath).getUsableSpace();
    }

    public long getDirectorySize(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) return 0;
        try (var stream = Files.walk(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        }
    }

    public List<File> findMediaFiles(String rootDir) {
        List<File> result = new ArrayList<>();
        File root = new File(rootDir);
        if (!root.exists() || !root.isDirectory()) {
            log.warn("Diretório não encontrado: {}", rootDir);
            return result;
        }
        scanDirectory(root, result);
        return result;
    }

    public boolean isValidMediaFile(File file) {
        if (!file.isFile()) return false;
        String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
        return MEDIA_EXTENSIONS.contains(ext);
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("Arquivo deletado: {}", filePath);
        }
    }

    private void ensureDirectoryExists(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
        } catch (IOException e) {
            log.error("Não foi possível criar diretório: {}", dirPath, e);
        }
    }

    private void scanDirectory(File dir, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, result);
            } else if (isValidMediaFile(file)) {
                result.add(file);
            }
        }
    }
}
