package kt.aivle.content.service;

import kt.aivle.content.entity.Content;
import kt.aivle.content.entity.ContentType;
import kt.aivle.content.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ContentService {

    private final ContentRepository contentRepository;
    private final S3Service s3Service;

    @Value("${app.supported.image-formats}")
    private String supportedImageFormats;

    @Value("${app.supported.video-formats}")
    private String supportedVideoFormats;

    @Value("${app.max-file-size.image}")
    private long maxImageSize;

    @Value("${app.max-file-size.video}")
    private long maxVideoSize;

    @Value("${pagination.default-page-size}")
    private int defaultPageSize;

    @Value("${pagination.max-page-size}")
    private int maxPageSize;

    public ContentService(ContentRepository contentRepository, S3Service s3Service) {
        this.contentRepository = contentRepository;
        this.s3Service = s3Service;
    }

    /**
     * 사용자별 전체 콘텐츠 목록 조회 (페이징)
     */
    public Page<Content> getContentsByUser(String userId, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return contentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자별 특정 타입 콘텐츠 목록 조회
     */
    public Page<Content> getContentsByUserAndType(String userId, ContentType type, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return contentRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
    }

    /**
     * 콘텐츠 상세 조회
     */
    public Optional<Content> getContentById(Long id) {
        return contentRepository.findById(id);
    }

    /**
     * 사용자의 콘텐츠 상세 조회 (권한 확인)
     */
    public Optional<Content> getContentByIdAndUser(Long id, String userId) {
        return contentRepository.findById(id)
                .filter(content -> content.getUserId().equals(userId));
    }

    /**
     * 제목으로 콘텐츠 검색
     */
    public Page<Content> searchContentsByTitle(String userId, String title, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return contentRepository.findByUserIdAndTitleContaining(userId, title, pageable);
    }

    /**
     * 날짜 범위로 콘텐츠 조회
     */
    public List<Content> getContentsByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return contentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    /**
     * 파일 크기 범위로 콘텐츠 조회
     */
    public Page<Content> getContentsByFileSizeRange(String userId, Long minSize, Long maxSize, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return contentRepository.findByUserIdAndFileSizeRange(userId, minSize, maxSize, pageable);
    }

    /**
     * 콘텐츠 삭제 (Hard Delete)
     */
    @Transactional
    public void deleteContent(Long id, String userId) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("콘텐츠를 찾을 수 없습니다."));

        // 디버깅 로그 추가
        System.out.println("=== 삭제 요청 디버깅 ===");
        System.out.println("요청 ID: " + id);
        System.out.println("요청 userId: " + userId);
        System.out.println("DB의 userId: " + content.getUserId());
        System.out.println("userId 같은가? " + content.getUserId().equals(userId));
        System.out.println("========================");

        // 사용자 권한 확인
//        if (!content.getUserId().equals(userId)) {
//            throw new RuntimeException("삭제 권한이 없습니다.");
//        }

        // S3에서 파일 삭제
        try {
            s3Service.deleteFile(content.getS3Key());

            // 썸네일이 있는 경우 썸네일도 삭제
            if (content instanceof kt.aivle.content.entity.Image) {
                kt.aivle.content.entity.Image image = (kt.aivle.content.entity.Image) content;
                if (image.getThumbnailS3Key() != null) {
                    s3Service.deleteFile(image.getThumbnailS3Key());
                }
            } else if (content instanceof kt.aivle.content.entity.Video) {
                kt.aivle.content.entity.Video video = (kt.aivle.content.entity.Video) content;
                if (video.getThumbnailS3Key() != null) {
                    s3Service.deleteFile(video.getThumbnailS3Key());
                }
            }
        } catch (Exception e) {
            // S3 삭제 실패 시 로그 남기고 계속 진행
            System.err.println("S3 파일 삭제 실패: " + e.getMessage());
        }

        // DB에서 삭제
        contentRepository.delete(content);
    }

    /**
     * 사용자 통계 정보 조회
     */
    public ContentStats getUserContentStats(String userId) {
        long totalCount = contentRepository.countByUserId(userId);
        long imageCount = contentRepository.countByUserIdAndType(userId, ContentType.IMAGE);
        long videoCount = contentRepository.countByUserIdAndType(userId, ContentType.VIDEO);
        Long totalFileSize = contentRepository.getTotalFileSizeByUserId(userId);

        return new ContentStats(totalCount, imageCount, videoCount, totalFileSize != null ? totalFileSize : 0L);
    }

    /**
     * 월별 업로드 통계
     */
    public List<Object[]> getMonthlyUploadStats(String userId) {
        return contentRepository.getMonthlyUploadStats(userId);
    }

    /**
     * 파일 유효성 검증
     */
    public void validateFile(MultipartFile file, ContentType expectedType) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일이 선택되지 않았습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("파일명이 올바르지 않습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();

        if (expectedType == ContentType.IMAGE) {
            validateImageFile(file, extension);
        } else if (expectedType == ContentType.VIDEO) {
            validateVideoFile(file, extension);
        }
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file, String extension) {
        List<String> supportedFormats = Arrays.asList(supportedImageFormats.split(","));
        if (!supportedFormats.contains(extension)) {
            throw new RuntimeException("지원하지 않는 이미지 형식입니다. 지원 형식: " + supportedImageFormats);
        }

        if (file.getSize() > maxImageSize) {
            throw new RuntimeException("이미지 파일 크기가 너무 큽니다. 최대 크기: " + (maxImageSize / 1024 / 1024) + "MB");
        }
    }

    /**
     * 영상 파일 유효성 검증
     */
    private void validateVideoFile(MultipartFile file, String extension) {
        List<String> supportedFormats = Arrays.asList(supportedVideoFormats.split(","));
        if (!supportedFormats.contains(extension)) {
            throw new RuntimeException("지원하지 않는 영상 형식입니다. 지원 형식: " + supportedVideoFormats);
        }

        if (file.getSize() > maxVideoSize) {
            throw new RuntimeException("영상 파일 크기가 너무 큽니다. 최대 크기: " + (maxVideoSize / 1024 / 1024) + "MB");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Pageable 객체 생성 (유효성 검증 포함)
     */
    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = defaultPageSize;
        }
        if (size > maxPageSize) {
            size = maxPageSize;
        }
        return PageRequest.of(page, size);
    }

    // === Inner Classes ===

    /**
     * 콘텐츠 통계 정보 클래스
     */
    public static class ContentStats {
        private final long totalCount;
        private final long imageCount;
        private final long videoCount;
        private final long totalFileSize;

        public ContentStats(long totalCount, long imageCount, long videoCount, long totalFileSize) {
            this.totalCount = totalCount;
            this.imageCount = imageCount;
            this.videoCount = videoCount;
            this.totalFileSize = totalFileSize;
        }

        public long getTotalCount() { return totalCount; }
        public long getImageCount() { return imageCount; }
        public long getVideoCount() { return videoCount; }
        public long getTotalFileSize() { return totalFileSize; }

        public String getFormattedFileSize() {
            if (totalFileSize < 1024) return totalFileSize + " B";
            if (totalFileSize < 1024 * 1024) return String.format("%.1f KB", totalFileSize / 1024.0);
            if (totalFileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", totalFileSize / (1024.0 * 1024.0));
            return String.format("%.1f GB", totalFileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
}