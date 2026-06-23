package pedroMartinsMJ.MyStreaming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pedroMartinsMJ.MyStreaming.exception.EncodingException;
import pedroMartinsMJ.MyStreaming.exception.VideoNotFoundException;
import pedroMartinsMJ.MyStreaming.model.*;
import pedroMartinsMJ.MyStreaming.repository.EncodedVideoRepository;
import pedroMartinsMJ.MyStreaming.repository.EncodingJobRepository;
import pedroMartinsMJ.MyStreaming.repository.VideoRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncodingService {

    private final EncodingJobRepository encodingJobRepository;
    private final EncodedVideoRepository encodedVideoRepository;
    private final VideoRepository videoRepository;
    private final FileSystemService fileSystemService;

    /**
     * Perfis de encoding pré-definidos: resolução e bitrate
     */
    public record EncodingProfile(String name, int width, int height, int bitrate, String codec) {}

    public static final List<EncodingProfile> DEFAULT_PROFILES = List.of(
            new EncodingProfile("1080p", 1920, 1080, 8000, "libx264"),
            new EncodingProfile("720p",  1280,  720, 5000, "libx264"),
            new EncodingProfile("480p",   854,  480, 3000, "libx264"),
            new EncodingProfile("360p",   640,  360, 1500, "libx264")
    );

    @Transactional
    public List<EncodingJob> startAdaptiveEncoding(UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        video.setStatus(VideoStatus.ENCODING);
        videoRepository.save(video);

        List<EncodingJob> jobs = new ArrayList<>();
        for (EncodingProfile profile : DEFAULT_PROFILES) {
            EncodingJob job = EncodingJob.builder()
                    .video(video)
                    .encodingType(EncodingType.ADAPTIVE)
                    .targetCodec("h264")
                    .targetBitrate(profile.bitrate())
                    .targetWidth(profile.width())
                    .targetHeight(profile.height())
                    .status(EncodingStatus.PENDING)
                    .priority(5)
                    .build();
            jobs.add(encodingJobRepository.save(job));
        }

        log.info("Criados {} jobs de encoding para vídeo {}", jobs.size(), videoId);

        // Processar cada job de forma assíncrona
        for (EncodingJob job : jobs) {
            processJobAsync(job.getId());
        }

        return jobs;
    }

    @Async
    @Transactional
    public void processJobAsync(UUID jobId) {
        EncodingJob job = encodingJobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        job.setStatus(EncodingStatus.PROCESSING);
        job.setStartedAt(LocalDateTime.now());
        encodingJobRepository.save(job);

        try {
            encodeWithFFmpeg(job);
            job.setStatus(EncodingStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setProgressPercentage(100.0);
            encodingJobRepository.save(job);
            log.info("Job de encoding {} concluído", jobId);
            checkAndUpdateVideoStatus(job.getVideo().getId());

        } catch (Exception e) {
            job.setStatus(EncodingStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            encodingJobRepository.save(job);
            log.error("Falha no job de encoding {}: {}", jobId, e.getMessage());
        }
    }

    private void encodeWithFFmpeg(EncodingJob job) throws Exception {
        String inputPath  = job.getVideo().getOriginalFilePath();
        String outputDir  = Paths.get(fileSystemService.getEncodedStoragePath(),
                job.getVideo().getId().toString()).toString();

        new File(outputDir).mkdirs();

        String hlsOutputPath = Paths.get(outputDir,
                job.getTargetBitrate() + "k").toString();
        new File(hlsOutputPath).mkdirs();

        String outputPattern = Paths.get(hlsOutputPath, "segment%03d.ts").toString();
        String playlistPath  = Paths.get(hlsOutputPath, "playlist.m3u8").toString();

        List<String> cmd = List.of(
                "ffmpeg", "-y",
                "-i", inputPath,
                "-c:v", job.getTargetCodec() != null ? "lib" + job.getTargetCodec() : "libx264",
                "-b:v", job.getTargetBitrate() + "k",
                "-vf", "scale=" + job.getTargetWidth() + ":" + job.getTargetHeight(),
                "-c:a", "aac", "-b:a", "128k",
                "-f", "hls",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-hls_segment_filename", outputPattern,
                playlistPath
        );

        log.info("Executando FFmpeg: {}", String.join(" ", cmd));

        Process process = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        // Salvar log
        job.setLogContent(output.substring(Math.max(0, output.length() - 2000)));
        encodingJobRepository.save(job);

        if (exitCode != 0) {
            throw new EncodingException("FFmpeg retornou código " + exitCode + " para job " + job.getId());
        }

        // Registrar versão encodada
        EncodedVideo encodedVideo = EncodedVideo.builder()
                .video(job.getVideo())
                .codec("h264")
                .audioCodec("aac")
                .bitrate(job.getTargetBitrate())
                .audioBitrate(128)
                .width(job.getTargetWidth())
                .height(job.getTargetHeight())
                .hlsBasePath(hlsOutputPath)
                .segmentDurationSeconds(10)
                .encodingStatus(EncodingStatus.COMPLETED)
                .build();

        encodedVideoRepository.save(encodedVideo);
    }

    private void checkAndUpdateVideoStatus(UUID videoId) {
        List<EncodingJob> allJobs = encodingJobRepository.findByVideoId(videoId);
        boolean allDone = allJobs.stream()
                .allMatch(j -> j.getStatus() == EncodingStatus.COMPLETED ||
                               j.getStatus() == EncodingStatus.FAILED);

        if (allDone) {
            boolean anySuccess = allJobs.stream()
                    .anyMatch(j -> j.getStatus() == EncodingStatus.COMPLETED);

            videoRepository.findById(videoId).ifPresent(video -> {
                video.setStatus(anySuccess ? VideoStatus.READY : VideoStatus.ERROR);
                videoRepository.save(video);
                log.info("Vídeo {} atualizado para status: {}", videoId, video.getStatus());
            });
        }
    }

    @Transactional(readOnly = true)
    public EncodingJob getJobStatus(UUID jobId) {
        return encodingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado: " + jobId));
    }

    @Transactional(readOnly = true)
    public List<EncodingJob> getPendingJobs() {
        return encodingJobRepository.findByStatus(EncodingStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<EncodingJob> getJobsByVideoId(UUID videoId) {
        return encodingJobRepository.findByVideoId(videoId);
    }

    @Transactional
    public void cancelJob(UUID jobId) {
        EncodingJob job = encodingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado: " + jobId));
        if (job.getStatus() == EncodingStatus.PENDING) {
            job.setStatus(EncodingStatus.CANCELLED);
            encodingJobRepository.save(job);
        }
    }
}
