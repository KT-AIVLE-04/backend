package kt.aivle.content.service;

import kt.aivle.content.config.FileStorageConfig;
import kt.aivle.content.domain.VideoContent;
import kt.aivle.content.dto.common.FileMetadata;
import kt.aivle.content.exception.FileProcessingException;
import kt.aivle.content.repository.VideoContentRepository;
import kt.aivle.content.util.VideoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingService {

    private final VideoContentRepository videoRepository;
    private final FileStorageConfig fileConfig;
    private final VideoUtils videoUtils;

    /**
     * 영상 압축 (비동기)
     */
    @Async
    public CompletableFuture<String> compressVideo(String inputPath, String outputPath, String quality) {
        log.info("Starting video compression: {} -> {}", inputPath, outputPath);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputPath,
                    "-c:v", "libx264",           // H.264 코덱
                    "-preset", "medium",         // 압축 속도/품질 균형
                    "-crf", quality,             // 품질 (18-28, 낮을수록 고품질)
                    "-c:a", "aac",              // 오디오 코덱
                    "-b:a", "128k",             // 오디오 비트레이트
                    "-movflags", "+faststart",   // 웹 최적화
                    "-y",                       // 덮어쓰기
                    outputPath
            );

            Process process = pb.start();

            // 진행 상황 로깅
            logFFmpegProgress(process);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Video compression completed successfully: {}", outputPath);
                return CompletableFuture.completedFuture(outputPath);
            } else {
                throw new FileProcessingException("영상 압축에 실패했습니다. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error during video compression", e);
            throw new FileProcessingException("영상 압축 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 영상을 여러 해상도로 변환 (적응적 스트리밍용)
     */
    @Async
    public CompletableFuture<List<String>> createMultipleResolutions(String inputPath, Long videoId) {
        log.info("Creating multiple resolutions for video: {}", inputPath);

        try {
            Path videoDir = Paths.get(fileConfig.getUploadDir(), fileConfig.getVideoDir(), "resolutions", String.valueOf(videoId));
            Files.createDirectories(videoDir);

            // 다양한 해상도 설정
            ResolutionConfig[] resolutions = {
                    new ResolutionConfig("1080p", 1920, 1080, "5000k"),
                    new ResolutionConfig("720p", 1280, 720, "2500k"),
                    new ResolutionConfig("480p", 854, 480, "1000k"),
                    new ResolutionConfig("360p", 640, 360, "600k")
            };

            List<CompletableFuture<String>> futures = List.of(resolutions).stream()
                    .map(config -> processResolution(inputPath, videoDir, config))
                    .toList();

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            return allOf.thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .toList());

        } catch (IOException e) {
            log.error("Error creating multiple resolutions", e);
            throw new FileProcessingException("다중 해상도 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 영상에서 특정 시간의 프레임 추출
     */
    public String extractFrameAtTime(String videoPath, int timeInSeconds, String outputPath) {
        log.debug("Extracting frame at {}s from: {}", timeInSeconds, videoPath);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-ss", String.valueOf(timeInSeconds),
                    "-vframes", "1",
                    "-q:v", "2",                // 고품질
                    "-y",
                    outputPath
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.debug("Frame extracted successfully: {}", outputPath);
                return outputPath;
            } else {
                throw new FileProcessingException("프레임 추출에 실패했습니다. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error extracting frame", e);
            throw new FileProcessingException("프레임 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 영상을 GIF로 변환
     */
    @Async
    public CompletableFuture<String> convertToGif(String videoPath, String outputPath, int startTime, int duration) {
        log.info("Converting video to GIF: {} -> {}", videoPath, outputPath);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-ss", String.valueOf(startTime),
                    "-t", String.valueOf(duration),
                    "-vf", "fps=10,scale=480:-1:flags=lanczos,palettegen=reserve_transparent=0",
                    "-y",
                    outputPath.replace(".gif", "_palette.png")
            );

            Process paletteProcess = pb.start();
            int paletteExitCode = paletteProcess.waitFor();

            if (paletteExitCode != 0) {
                throw new FileProcessingException("GIF 팔레트 생성에 실패했습니다");
            }

            // GIF 생성
            ProcessBuilder gifPb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-i", outputPath.replace(".gif", "_palette.png"),
                    "-ss", String.valueOf(startTime),
                    "-t", String.valueOf(duration),
                    "-lavfi", "fps=10,scale=480:-1:flags=lanczos[x];[x][1:v]paletteuse",
                    "-y",
                    outputPath
            );

            Process gifProcess = gifPb.start();
            int gifExitCode = gifProcess.waitFor();

            if (gifExitCode == 0) {
                // 임시 팔레트 파일 삭제
                Files.deleteIfExists(Paths.get(outputPath.replace(".gif", "_palette.png")));

                log.info("GIF conversion completed: {}", outputPath);
                return CompletableFuture.completedFuture(outputPath);
            } else {
                throw new FileProcessingException("GIF 변환에 실패했습니다. Exit code: " + gifExitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error converting to GIF", e);
            throw new FileProcessingException("GIF 변환 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 영상 워터마크 추가
     */
    public String addWatermark(String videoPath, String watermarkPath, String outputPath, String position) {
        log.info("Adding watermark to video: {}", videoPath);

        try {
            String overlayFilter = getWatermarkPosition(position);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-i", watermarkPath,
                    "-filter_complex", overlayFilter,
                    "-codec:a", "copy",
                    "-y",
                    outputPath
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Watermark added successfully: {}", outputPath);
                return outputPath;
            } else {
                throw new FileProcessingException("워터마크 추가에 실패했습니다. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error adding watermark", e);
            throw new FileProcessingException("워터마크 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 썸네일이 없는 영상들의 썸네일 일괄 생성
     */
    @Async
    @Transactional
    public CompletableFuture<Void> generateMissingThumbnails() {
        log.info("Starting batch thumbnail generation");

        List<VideoContent> videosWithoutThumbnails = videoRepository.findByThumbnailPathIsNullAndIsDeletedFalse();

        for (VideoContent video : videosWithoutThumbnails) {
            try {
                String thumbnailPath = videoUtils.generateVideoThumbnail(video.getFilePath());
                video.setThumbnailPath(thumbnailPath);
                videoRepository.save(video);

                log.debug("Thumbnail generated for video ID: {}", video.getId());

            } catch (Exception e) {
                log.error("Failed to generate thumbnail for video ID: {}", video.getId(), e);
            }
        }

        log.info("Batch thumbnail generation completed. Processed {} videos", videosWithoutThumbnails.size());
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 영상 메타데이터 업데이트 (일괄 처리)
     */
    @Async
    @Transactional
    public CompletableFuture<Void> updateVideoMetadata() {
        log.info("Starting batch metadata update");

        List<VideoContent> videos = videoRepository.findByIsDeletedFalse();

        for (VideoContent video : videos) {
            try {
                FileMetadata metadata = videoUtils.extractMetadata(video.getFilePath());

                if (metadata.getWidth() != null) video.setWidth(metadata.getWidth());
                if (metadata.getHeight() != null) video.setHeight(metadata.getHeight());
                if (metadata.getDuration() != null) video.setDuration(metadata.getDuration());
                if (metadata.getBitrate() != null) video.setBitrate(metadata.getBitrate());
                if (metadata.getFrameRate() != null) video.setFrameRate(metadata.getFrameRate());

                videoRepository.save(video);

                log.debug("Metadata updated for video ID: {}", video.getId());

            } catch (Exception e) {
                log.error("Failed to update metadata for video ID: {}", video.getId(), e);
            }
        }

        log.info("Batch metadata update completed. Processed {} videos", videos.size());
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<String> processResolution(String inputPath, Path outputDir, ResolutionConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String outputPath = outputDir.resolve(config.name + ".mp4").toString();

                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg",
                        "-i", inputPath,
                        "-c:v", "libx264",
                        "-b:v", config.bitrate,
                        "-vf", String.format("scale=%d:%d", config.width, config.height),
                        "-c:a", "aac",
                        "-b:a", "128k",
                        "-y",
                        outputPath
                );

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    log.debug("Resolution {} created successfully", config.name);
                    return outputPath;
                } else {
                    throw new FileProcessingException("해상도 " + config.name + " 생성에 실패했습니다");
                }

            } catch (IOException | InterruptedException e) {
                log.error("Error processing resolution: {}", config.name, e);
                throw new FileProcessingException("해상도 처리 중 오류: " + e.getMessage());
            }
        });
    }

    private void logFFmpegProgress(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("time=")) {
                        log.debug("FFmpeg progress: {}", line.trim());
                    }
                }
            } catch (IOException e) {
                log.warn("Error reading FFmpeg output", e);
            }
        }).start();
    }

    private String getWatermarkPosition(String position) {
        return switch (position.toLowerCase()) {
            case "top-left" -> "[0:v][1:v]overlay=10:10";
            case "top-right" -> "[0:v][1:v]overlay=W-w-10:10";
            case "bottom-left" -> "[0:v][1:v]overlay=10:H-h-10";
            case "bottom-right" -> "[0:v][1:v]overlay=W-w-10:H-h-10";
            case "center" -> "[0:v][1:v]overlay=(W-w)/2:(H-h)/2";
            default -> "[0:v][1:v]overlay=W-w-10:H-h-10"; // 기본값: 우하단
        };
    }

    /**
     * 해상도 설정 클래스
     */
    private static class ResolutionConfig {
        final String name;
        final int width;
        final int height;
        final String bitrate;

        ResolutionConfig(String name, int width, int height, String bitrate) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.bitrate = bitrate;
        }
    }
}