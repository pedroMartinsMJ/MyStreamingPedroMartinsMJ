package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pedroMartinsMJ.MyStreaming.model.User;
import pedroMartinsMJ.MyStreaming.model.Video;
import pedroMartinsMJ.MyStreaming.model.VideoStatus;
import pedroMartinsMJ.MyStreaming.repository.UserRepository;
import pedroMartinsMJ.MyStreaming.repository.VideoRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FileSystemService fileSystemService;

    /**
     * Varre um diretório e importa todos os arquivos de mídia encontrados.
     */
    @Transactional
    public List<Video> scanDirectory(String dirPath, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        List<File> mediaFiles = fileSystemService.findMediaFiles(dirPath);
        List<Video> imported = new ArrayList<>();

        for (File file : mediaFiles) {
            String filePath = file.getAbsolutePath();

            // Verificar se já foi importado
            if (videoRepository.findByOriginalFilePath(filePath).isPresent()) {
                log.debug("Vídeo já existente, ignorando: {}", filePath);
                continue;
            }

            String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
            if (!isVideoExtension(ext)) continue;

            Video video = Video.builder()
                    .title(FilenameUtils.getBaseName(file.getName()))
                    .user(user)
                    .originalFilePath(filePath)
                    .originalFormat(ext)
                    .fileSize(file.length())
                    .status(VideoStatus.UPLOADED)
                    .build();

            imported.add(videoRepository.save(video));
        }

        log.info("Scan concluído: {} novos vídeos importados de '{}'", imported.size(), dirPath);
        return imported;
    }

    @Transactional(readOnly = true)
    public List<Video> getUserLibrary(UUID userId) {
        return videoRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Video> search(String query) {
        return videoRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<Video> getByGenre(String genre) {
        return videoRepository.findByGenre(genre);
    }

    @Transactional(readOnly = true)
    public List<Video> getReadyVideos() {
        return videoRepository.findByStatus(VideoStatus.READY);
    }

    private boolean isVideoExtension(String ext) {
        return List.of("mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "m4v").contains(ext);
    }
}
