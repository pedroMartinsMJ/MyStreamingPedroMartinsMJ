package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pedroMartinsMJ.MyStreaming.exception.StorageException;
import pedroMartinsMJ.MyStreaming.exception.VideoNotFoundException;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;
import pedroMartinsMJ.MyStreaming.repository.UserRepository;
import pedroMartinsMJ.MyStreaming.repository.VideoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "m4v"
    );

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FileSystemService fileSystemService;

    @Transactional(readOnly = true)
    public Page<Video> getAllVideos(Pageable pageable) {
        return videoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Video getVideoById(UUID videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));
    }

    @Transactional(readOnly = true)
    public List<Video> searchVideos(String query) {
        return videoRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<Video> getVideosByGenre(String genre) {
        return videoRepository.findByGenre(genre);
    }

    @Transactional(readOnly = true)
    public List<Video> getVideosByUser(UUID userId) {
        return videoRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Video> getVideosByStatus(VideoStatus status) {
        return videoRepository.findByStatus(status);
    }

    @Transactional
    public Video uploadVideo(MultipartFile file, String title, String description, UUID userId) {
        validateVideoFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        String storedFilename = UUID.randomUUID() + "." + extension;
        String storagePath = fileSystemService.getVideoStoragePath();

        try {
            Path targetPath = Paths.get(storagePath, storedFilename);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());

            Video video = Video.builder()
                    .title(title != null ? title : FilenameUtils.getBaseName(originalFilename))
                    .description(description)
                    .user(user)
                    .originalFilePath(targetPath.toString())
                    .originalFormat(extension)
                    .fileSize(file.getSize())
                    .status(VideoStatus.UPLOADED)
                    .build();

            Video saved = videoRepository.save(video);
            log.info("Vídeo '{}' salvo: {}", saved.getTitle(), targetPath);
            return saved;

        } catch (IOException e) {
            throw new StorageException("Falha ao salvar arquivo de vídeo", e);
        }
    }

    @Transactional
    public Video importFromPath(String filePath, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        if (videoRepository.findByOriginalFilePath(filePath).isPresent()) {
            throw new IllegalArgumentException("Vídeo já importado: " + filePath);
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new StorageException("Arquivo não encontrado: " + filePath);
        }

        String extension = FilenameUtils.getExtension(filePath).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException("Formato não suportado: " + extension);
        }

        try {
            Video video = Video.builder()
                    .title(FilenameUtils.getBaseName(filePath))
                    .user(user)
                    .originalFilePath(filePath)
                    .originalFormat(extension)
                    .fileSize(Files.size(path))
                    .status(VideoStatus.UPLOADED)
                    .build();

            Video saved = videoRepository.save(video);
            log.info("Vídeo importado: {}", filePath);
            return saved;

        } catch (IOException e) {
            throw new StorageException("Erro ao ler arquivo: " + filePath, e);
        }
    }

    @Transactional
    public Video updateVideo(UUID videoId, String title, String description, String genre,
                              String director, Integer releaseYear) {
        Video video = getVideoById(videoId);

        if (title != null) video.setTitle(title);
        if (description != null) video.setDescription(description);
        if (genre != null) video.setGenre(genre);
        if (director != null) video.setDirector(director);
        if (releaseYear != null) video.setReleaseYear(releaseYear);

        return videoRepository.save(video);
    }

    @Transactional
    public void deleteVideo(UUID videoId) {
        Video video = getVideoById(videoId);
        video.setStatus(VideoStatus.DELETED);
        videoRepository.save(video);
        log.info("Vídeo marcado como deletado: {}", videoId);
    }

    @Transactional
    public void recordAccess(UUID videoId) {
        videoRepository.findById(videoId).ifPresent(video -> {
            video.setLastAccessedAt(LocalDateTime.now());
            video.setTimesWatched(video.getTimesWatched() + 1);
            videoRepository.save(video);
        });
    }

    @Transactional
    public void updateStatus(UUID videoId, VideoStatus status) {
        Video video = getVideoById(videoId);
        video.setStatus(status);
        videoRepository.save(video);
    }

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de vídeo não pode ser vazio");
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException("Formato não suportado: " + extension +
                    ". Suportados: " + SUPPORTED_FORMATS);
        }
    }
}
