package kt.aivle.content.service;

import kt.aivle.content.entity.ContentType;
import kt.aivle.content.entity.Video;
import kt.aivle.content.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VideoService {

    private final VideoRepository videoRepository;
    private final ContentService contentService;
    private final S3Service s3Service;

    public VideoService(VideoRepository videoRepository, ContentService contentService, S3Service s3Service) {
        this.videoRepository = videoRepository;
        this.contentService = contentService;
        this.s3Service = s3Service;
    }

    /**
     * 영상 업로드 및 저장
     */
    @Transactional
    public Video uploadVideo(MultipartFile file, String title, String userId) {
        // 파일 유효성 검증
        contentService.validateFile(file, ContentType.VIDEO);

        try {
            // S3에 영상 업로드
            S3Service.S3UploadResult uploadResult = s3Service.uploadFile(file, S3Service.FOLDER_VIDEOS);

            // Video 엔티티 생성
            Video video = Video.createVideo(
                    title != null && !title.trim().isEmpty() ? title : getDefaultTitle(file.getOriginalFilename()),
                    file.getOriginalFilename(),
                    uploadResult.getS3Url(),
                    uploadResult.getS3Key(),
                    uploadResult.getFileSize(),
                    file.getContentType(),
                    userId
            );

            // 영상 저장
            Video savedVideo = videoRepository.save(video);

            // 영상 메타데이터 추출 및 썸네일 생성 (비동기로 처리할 수도 있음)
            try {
                processVideoMetadata(savedVideo);
            } catch (Exception e) {
                // 메타데이터 처리 실패 시 로그만 남기고 계속 진행
                System.err.println("영상 메타데이터 처리 실패: " + e.getMessage());
            }

            return savedVideo;

        } catch (Exception e) {
            throw new RuntimeException("영상 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 영상 목록 조회
     */
    public Page<Video> getVideosByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자별 숏츠 영상 목록 조회
     */
    public Page<Video> getShortsByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndIsShortTrueOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자별 일반 영상 목록 조회 (숏츠 제외)
     */
    public Page<Video> getRegularVideosByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndIsShortFalseOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 영상 상세 조회
     */
    public Optional<Video> getVideoById(Long id) {
        return videoRepository.findById(id);
    }

    /**
     * 사용자의 영상 상세 조회 (권한 확인)
     */
    public Optional<Video> getVideoByIdAndUser(Long id, String userId) {
        return videoRepository.findById(id)
                .filter(video -> video.getUserId().equals(userId));
    }

    /**
     * 영상 길이별 조회
     */
    public Page<Video> getVideosByDuration(String userId, Integer minDuration, Integer maxDuration, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndDurationRange(userId, minDuration, maxDuration, pageable);
    }

    /**
     * 해상도별 영상 조회
     */
    public Page<Video> getVideosByResolution(String userId, Integer minWidth, Integer minHeight, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndMinResolution(userId, minWidth, minHeight, pageable);
    }

    /**
     * 코덱별 영상 조회
     */
    public Page<Video> getVideosByCodec(String userId, String codec, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndCodecOrderByCreatedAtDesc(userId, codec, pageable);
    }

    /**
     * 비트레이트별 영상 조회
     */
    public Page<Video> getVideosByBitrate(String userId, Integer minBitrate, Integer maxBitrate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndBitrateRange(userId, minBitrate, maxBitrate, pageable);
    }

    /**
     * 파일명으로 영상 검색
     */
    public Page<Video> searchVideosByFilename(String userId, String filename, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndOriginalFilenameContaining(userId, filename, pageable);
    }

    /**
     * 최근 업로드된 영상 조회 (미리보기용)
     */
    public List<Video> getRecentVideos(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return videoRepository.findRecentVideosByUserId(userId, pageable);
    }

    /**
     * 해상도별 통계
     */
    public List<Object[]> getResolutionStats(String userId) {
        return videoRepository.getResolutionStats(userId);
    }

    /**
     * 영상 길이별 통계
     */
    public List<Object[]> getDurationStats(String userId) {
        return videoRepository.getDurationStats(userId);
    }

    /**
     * 사용자의 총 영상 재생 시간
     */
    public Long getTotalDuration(String userId) {
        return videoRepository.getTotalDurationByUserId(userId);
    }

    /**
     * 사용자의 총 영상 재생 시간 (포맷팅)
     */
    public String getFormattedTotalDuration(String userId) {
        Long totalSeconds = getTotalDuration(userId);
        if (totalSeconds == null || totalSeconds == 0) {
            return "0분";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }

    /**
     * 영상 삭제
     */
    @Transactional
    public void deleteVideo(Long id, String userId) {
        contentService.deleteContent(id, userId);
    }

    /**
     * 썸네일이 없는 영상들에 대해 썸네일 생성 (배치 작업)
     */
    @Transactional
    public void generateMissingThumbnails() {
        List<Video> videosWithoutThumbnail = videoRepository.findVideosWithoutThumbnail();

        for (Video video : videosWithoutThumbnail) {
            try {
                generateVideoThumbnail(video);
                videoRepository.save(video);
            } catch (Exception e) {
                System.err.println("영상 " + video.getId() + " 썸네일 생성 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 영상 압축 (실제 구현에서는 FFmpeg 등 사용)
     */
    @Transactional
    public Video compressVideo(Long videoId, String userId, String quality) {
        Video video = getVideoByIdAndUser(videoId, userId)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없거나 권한이 없습니다."));

        try {
            // 실제 구현에서는 FFmpeg를 사용하여 영상 압축
            // 여기서는 간단히 메타데이터만 업데이트
            String compressedS3Key = video.getS3Key().replace(".", "_compressed.");
            String compressedUrl = video.getS3Url().replace(".", "_compressed.");

            // 압축된 영상을 새로운 파일로 저장하는 로직이 들어갈 자리
            // 실제로는 압축 작업이 완료된 후 업데이트

            return video;

        } catch (Exception e) {
            throw new RuntimeException("영상 압축 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // === Private Helper Methods ===

    /**
     * 영상 메타데이터 처리
     */
    private void processVideoMetadata(Video video) {
        try {
            // 실제 구현에서는 FFprobe 등을 사용하여 영상 메타데이터 추출
            // 여기서는 예시 데이터로 설정

            // 임시로 기본값 설정 (실제로는 FFprobe 결과 사용)
            video.updateMetadata(
                    null, // duration - FFprobe로 추출
                    1920, // width - 예시값
                    1080, // height - 예시값
                    5000, // bitrate - 예시값
                    30.0, // frameRate - 예시값
                    "h264" // codec - 예시값
            );

            // 썸네일 생성
            generateVideoThumbnail(video);

            videoRepository.save(video);

        } catch (Exception e) {
            System.err.println("영상 메타데이터 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 영상 썸네일 생성 (0:01초 지점 캡처)
     */
    private void generateVideoThumbnail(Video video) {
        try {
            // 실제 구현에서는 FFmpeg를 사용하여 0:01초 지점 이미지 캡처
            // 여기서는 간단히 더미 썸네일 URL 설정

            String thumbnailFilename = "video_thumb_" + System.currentTimeMillis() + ".jpg";
            String thumbnailS3Key = S3Service.FOLDER_THUMBNAILS + "/" + thumbnailFilename;
            String thumbnailUrl = "https://your-bucket.s3.region.amazonaws.com/" + thumbnailS3Key;

            // 실제로는 FFmpeg로 캡처한 이미지를 S3에 업로드
            video.updateThumbnail(thumbnailUrl, thumbnailS3Key);

        } catch (Exception e) {
            System.err.println("영상 썸네일 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 기본 제목 생성
     */
    private String getDefaultTitle(String originalFilename) {
        if (originalFilename == null) return "제목 없음";

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return originalFilename.substring(0, lastDotIndex);
        }
        return originalFilename;
    }

    /**
     * 영상 길이를 사람이 읽기 쉬운 형태로 변환
     */
    public String formatDuration(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "00:00";
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 변환
     */
    public String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return String.format("%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }
}