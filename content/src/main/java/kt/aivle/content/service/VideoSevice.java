package kt.aivle.content.service;

import kt.aivle.content.dto.VideoDto;
import kt.aivle.content.entity.Video;
import kt.aivle.content.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Client s3Client;
    private final ThumbnailService thumbnailService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.base-url}")
    private String s3BaseUrl;

    // 지원 파일 형식
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("mp4", "mov", "avi", "wmv");

    // 최대 파일 크기 (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 영상 업로드
     */
    public VideoDto uploadVideo(MultipartFile file, String title, String scenario) {
        validateFile(file);

        try {
            // S3에 파일 업로드
            String s3Key = generateS3Key(file.getOriginalFilename());
            String videoUrl = uploadToS3(file, s3Key);

            // 썸네일 생성 (0:01초 캡처)
            String thumbnailUrl = generateThumbnail(videoUrl, s3Key);

            // 영상 메타데이터 추출 (실제로는 FFmpeg 등 사용)
            VideoMetadata metadata = extractVideoMetadata(file);

            // DB에 메타데이터 저장
            Video video = Video.builder()
                    .scenario(scenario)
                    .originalFilename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .videoUrl(videoUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .s3Key(s3Key)
                    .contentType(file.getContentType())
                    .duration(metadata.getDuration())
                    .resolution(metadata.getResolution())
                    .frameRate(metadata.getFrameRate())
                    .bitrate(metadata.getBitrate())
                    .createdAt(LocalDateTime.now())
                    .build();

            Video savedVideo = videoRepository.save(video);
            log.info("영상 업로드 완료: {}", savedVideo.getId());

            return convertToDto(savedVideo);

        } catch (Exception e) {
            log.error("영상 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("영상 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 영상 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<VideoDto> getVideoList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Video> videoPage = videoRepository.findAll(pageable);

        return videoPage.map(this::convertToDto);
    }

    /**
     * 영상 상세 조회
     */
    @Transactional(readOnly = true)
    public VideoDto getVideoDetail(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다."));

        return convertToDto(video);
    }

    /**
     * 영상 삭제 (Hard Delete)
     */
    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다."));

        try {
            // S3에서 원본 영상 삭제
            deleteFromS3(video.getS3Key());

            // 썸네일도 삭제
            if (video.getThumbnailUrl() != null) {
                String thumbnailKey = extractS3KeyFromUrl(video.getThumbnailUrl());
                deleteFromS3(thumbnailKey);
            }

            // DB에서 삭제
            videoRepository.delete(video);
            log.info("영상 삭제 완료: {}", videoId);

        } catch (Exception e) {
            log.error("영상 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("영상 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 제목 또는 시나리오로 영상 검색
     */
    @Transactional(readOnly = true)
    public Page<VideoDto> searchVideos(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Video> videoPage = videoRepository.findByTitleContainingIgnoreCaseOrScenarioContainingIgnoreCase(
                keyword, keyword, pageable);

        return videoPage.map(this::convertToDto);
    }

    /**
     * 영상 압축 (선택적 기능)
     */
    public VideoDto compressVideo(Long videoId, String quality) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다."));

        try {
            // 실제로는 FFmpeg 등을 사용한 영상 압축 로직 필요
            String compressedS3Key = generateCompressedS3Key(video.getS3Key(), quality);
            String compressedUrl = performVideoCompression(video.getVideoUrl(), compressedS3Key, quality);

            // 압축된 영상으로 URL 업데이트
            video.updateVideoUrl(compressedUrl);
            video.updateS3Key(compressedS3Key);

            Video savedVideo = videoRepository.save(video);
            log.info("영상 압축 완료: {} -> {}", videoId, quality);

            return convertToDto(savedVideo);

        } catch (Exception e) {
            log.error("영상 압축 실패: {}", e.getMessage(), e);
            throw new RuntimeException("영상 압축 중 오류가 발생했습니다.", e);
        }
    }

    // === Private Methods ===

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. (최대 100MB)");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (지원: MP4, MOV, AVI, WMV)");
        }
    }

    /**
     * S3 키 생성
     */
    private String generateS3Key(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return "videos/" + LocalDateTime.now().toLocalDate() + "/" + uuid + "." + extension;
    }

    /**
     * 압축된 영상용 S3 키 생성
     */
    private String generateCompressedS3Key(String originalKey, String quality) {
        String extension = getFileExtension(originalKey);
        String baseKey = originalKey.substring(0, originalKey.lastIndexOf('.'));
        return baseKey + "_" + quality + "." + extension;
    }

    /**
     * S3에 파일 업로드
     */
    private String uploadToS3(MultipartFile file, String s3Key) throws IOException {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return s3BaseUrl + "/" + s3Key;
    }

    /**
     * 영상 썸네일 생성 (0:01초 캡처)
     */
    private String generateThumbnail(String videoUrl, String videoS3Key) {
        try {
            // 실제로는 FFmpeg 등을 사용해 0:01초 프레임 캡처
            String thumbnailKey = videoS3Key.replace("videos/", "thumbnails/")
                    .replace(getFileExtension(videoS3Key), "jpg");

            // ThumbnailService를 통해 썸네일 생성
            return thumbnailService.generateVideoThumbnail(videoUrl, thumbnailKey, 1); // 1초 지점

        } catch (Exception e) {
            log.error("썸네일 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 영상 메타데이터 추출
     */
    private VideoMetadata extractVideoMetadata(MultipartFile file) {
        // 실제로는 FFmpeg나 다른 라이브러리를 사용해 메타데이터 추출
        // 여기서는 기본값 반환 (데모용)
        return VideoMetadata.builder()
                .duration(0L)  // 실제로는 초 단위
                .resolution("1920x1080")
                .frameRate(30.0)
                .bitrate(2000L)  // kbps
                .build();
    }

    /**
     * 영상 압축 수행
     */
    private String performVideoCompression(String originalUrl, String compressedS3Key, String quality) {
        // 실제로는 FFmpeg 등을 사용한 영상 압축 로직
        // 여기서는 원본 URL 반환 (데모용)
        return s3BaseUrl + "/" + compressedS3Key;
    }

    /**
     * S3에서 파일 삭제
     */
    private void deleteFromS3(String s3Key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    /**
     * URL에서 S3 키 추출
     */
    private String extractS3KeyFromUrl(String url) {
        return url.replace(s3BaseUrl + "/", "");
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    /**
     * Entity를 DTO로 변환
     */
    private VideoDto convertToDto(Video video) {
        return VideoDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .scenario(truncateScenario(video.getScenario()))  // 목록에서는 제한
                .fullScenario(video.getScenario())  // 상세에서는 전체
                .originalFilename(video.getOriginalFilename())
                .fileSize(video.getFileSize())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .contentType(video.getContentType())
                .duration(video.getDuration())
                .resolution(video.getResolution())
                .frameRate(video.getFrameRate())
                .bitrate(video.getBitrate())
                .createdAt(video.getCreatedAt())
                .build();
    }

    /**
     * 시나리오 텍스트 자르기 (목록용)
     */
    private String truncateScenario(String scenario) {
        if (scenario == null) return null;
        return scenario.length() > 100 ? scenario.substring(0, 100) + "..." : scenario;
    }

    // 내부 클래스: 영상 메타데이터
    @lombok.Builder
    @lombok.Data
    private static class VideoMetadata {
        private Long duration;      // 초 단위
        private String resolution;  // 예: "1920x1080"
        private Double frameRate;   // fps
        private Long bitrate;       // kbps
    }
}