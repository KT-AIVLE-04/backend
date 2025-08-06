package kt.aivle.content.repository;

import kt.aivle.content.entity.Image;
import kt.aivle.content.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 커스텀 미디어 레포지토리 인터페이스
 * 영상과 이미지를 통합하여 조회하는 고급 기능 제공
 */
public interface CustomMediaRepository {

    /**
     * 사용자의 전체 미디어 파일 통계 조회
     */
    MediaStatistics getMediaStatisticsByUserId(Long userId);

    /**
     * 사용자별 최근 업로드된 미디어 파일들 조회 (영상 + 이미지 통합)
     */
    List<Object> getRecentMediaByUserId(Long userId, int limit);

    /**
     * 기간별 업로드 통계
     */
    List<DailyUploadStats> getDailyUploadStats(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 파일 크기별 분포 조회
     */
    List<FileSizeDistribution> getFileSizeDistribution(Long userId);

    /**
     * 사용자 저장소 사용량 상세 정보
     */
    StorageUsageDetail getStorageUsageDetail(Long userId);

    // === 내부 클래스들 ===

    /**
     * 미디어 통계 DTO
     */
    class MediaStatistics {
        private Long totalVideos;
        private Long totalImages;
        private Long totalFileSize;
        private Long completedVideos;
        private Long completedImages;
        private Long processingVideos;
        private Long processingImages;

        public MediaStatistics(Long totalVideos, Long totalImages, Long totalFileSize,
                               Long completedVideos, Long completedImages,
                               Long processingVideos, Long processingImages) {
            this.totalVideos = totalVideos;
            this.totalImages = totalImages;
            this.totalFileSize = totalFileSize;
            this.completedVideos = completedVideos;
            this.completedImages = completedImages;
            this.processingVideos = processingVideos;
            this.processingImages = processingImages;
        }

        // Getters
        public Long getTotalVideos() { return totalVideos; }
        public Long getTotalImages() { return totalImages; }
        public Long getTotalFiles() { return totalVideos + totalImages; }
        public Long getTotalFileSize() { return totalFileSize; }
        public Long getCompletedVideos() { return completedVideos; }
        public Long getCompletedImages() { return completedImages; }
        public Long getProcessingVideos() { return processingVideos; }
        public Long getProcessingImages() { return processingImages; }
    }

    /**
     * 일별 업로드 통계 DTO
     */
    class DailyUploadStats {
        private LocalDateTime date;
        private Long videoCount;
        private Long imageCount;
        private Long totalSize;

        public DailyUploadStats(LocalDateTime date, Long videoCount, Long imageCount, Long totalSize) {
            this.date = date;
            this.videoCount = videoCount;
            this.imageCount = imageCount;
            this.totalSize = totalSize;
        }

        // Getters
        public LocalDateTime getDate() { return date; }
        public Long getVideoCount() { return videoCount; }
        public Long getImageCount() { return imageCount; }
        public Long getTotalCount() { return videoCount + imageCount; }
        public Long getTotalSize() { return totalSize; }
    }

    /**
     * 파일 크기 분포 DTO
     */
    class FileSizeDistribution {
        private String sizeRange;
        private Long videoCount;
        private Long imageCount;

        public FileSizeDistribution(String sizeRange, Long videoCount, Long imageCount) {
            this.sizeRange = sizeRange;
            this.videoCount = videoCount;
            this.imageCount = imageCount;
        }

        // Getters
        public String getSizeRange() { return sizeRange; }
        public Long getVideoCount() { return videoCount; }
        public Long getImageCount() { return imageCount; }
        public Long getTotalCount() { return videoCount + imageCount; }
    }

    /**
     * 저장소 사용량 상세 DTO
     */
    class StorageUsageDetail {
        private Long totalSize;
        private Long videoSize;
        private Long imageSize;
        private Long thumbnailSize;
        private Double utilizationRate;

        public StorageUsageDetail(Long totalSize, Long videoSize, Long imageSize, Long thumbnailSize) {
            this.totalSize = totalSize;
            this.videoSize = videoSize;
            this.imageSize = imageSize;
            this.thumbnailSize = thumbnailSize;
            // 가정: 최대 용량 10GB
            this.utilizationRate = totalSize > 0 ? (totalSize.doubleValue() / (10L * 1024 * 1024 * 1024)) * 100 : 0.0;
        }

        // Getters
        public Long getTotalSize() { return totalSize; }
        public Long getVideoSize() { return videoSize; }
        public Long getImageSize() { return imageSize; }
        public Long getThumbnailSize() { return thumbnailSize; }
        public Double getUtilizationRate() { return utilizationRate; }
    }
}