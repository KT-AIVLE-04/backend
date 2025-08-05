package kt.aivle.content.mapper;

import kt.aivle.content.domain.ImageContent;
import kt.aivle.content.domain.VideoContent;
import kt.aivle.content.dto.common.ContentDto;
import kt.aivle.content.dto.response.*;
import kt.aivle.content.util.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class ContentMapper {

    /**
     * VideoContent -> VideoContentDto 변환
     */
    public VideoContentDto toVideoDto(VideoContent video) {
        if (video == null) {
            return null;
        }

        return VideoContentDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .isAiGenerated(video.getIsAiGenerated())
                .createdDate(video.getCreatedDate())
                .modifiedDate(video.getModifiedDate())
                .fileName(video.getFileName())
                .fileSize(video.getFileSize())
                .thumbnailPath(video.getThumbnailPath())
                .duration(video.getDuration())
                .videoFormat(video.getVideoFormat())
                .resolution(video.getResolution())
                .isShorts(video.getIsShorts())
                .scenario(truncateScenario(video.getScenario())) // 글자수 제한
                .build();
    }

    /**
     * VideoContent -> VideoContentDetailDto 변환
     */
    public VideoContentDetailDto toVideoDetailDto(VideoContent video) {
        if (video == null) {
            return null;
        }

        return VideoContentDetailDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .scenario(video.getScenario())
                .isAiGenerated(video.getIsAiGenerated())
                .createdDate(video.getCreatedDate())
                .modifiedDate(video.getModifiedDate())

                // 파일 정보
                .fileName(video.getFileName())
                .filePath(video.getFilePath())
                .fileSize(video.getFileSize())
                .contentType(video.getContentType())

                // 영상 정보
                .duration(video.getDuration())
                .videoFormat(video.getVideoFormat())
                .width(video.getWidth())
                .height(video.getHeight())
                .resolution(video.getResolution())
                .bitrate(video.getBitrate())
                .frameRate(video.getFrameRate())
                .isShorts(video.getIsShorts())
                .thumbnailPath(video.getThumbnailPath())

                // 포맷팅된 정보
                .formattedDuration(formatDuration(video.getDuration()))
                .formattedFileSize(FileUtils.formatFileSize(video.getFileSize()))
                .qualityLabel(getQualityLabel(video.getWidth(), video.getHeight()))
                .build();
    }

    /**
     * ImageContent -> ImageContentDto 변환
     */
    public ImageContentDto toImageDto(ImageContent image) {
        if (image == null) {
            return null;
        }

        return ImageContentDto.builder()
                .id(image.getId())
                .title(image.getTitle())
                .keywords(image.getKeywords())
                .isAiGenerated(image.getIsAiGenerated())
                .createdDate(image.getCreatedDate())
                .modifiedDate(image.getModifiedDate())
                .fileName(image.getFileName())
                .fileSize(image.getFileSize())
                .filePath(image.getFilePath())
                .imageFormat(image.getImageFormat())
                .resolution(image.getResolution())
                .isCompressed(image.getIsCompressed())
                .build();
    }

    /**
     * ImageContent -> ImageContentDetailDto 변환
     */
    public ImageContentDetailDto toImageDetailDto(ImageContent image) {
        if (image == null) {
            return null;
        }

        return ImageContentDetailDto.builder()
                .id(image.getId())
                .title(image.getTitle())
                .keywords(image.getKeywords())
                .scenario(image.getScenario())
                .isAiGenerated(image.getIsAiGenerated())
                .createdDate(image.getCreatedDate())
                .modifiedDate(image.getModifiedDate())

                // 파일 정보
                .fileName(image.getFileName())
                .filePath(image.getFilePath())
                .fileSize(image.getFileSize())
                .contentType(image.getContentType())
                .originalFileName(image.getOriginalFileName())

                // 이미지 정보
                .imageFormat(image.getImageFormat())
                .width(image.getWidth())
                .height(image.getHeight())
                .resolution(image.getResolution())
                .colorSpace(image.getColorSpace())
                .dpi(image.getDpi())
                .isCompressed(image.getIsCompressed())

                // 포맷팅된 정보
                .formattedFileSize(FileUtils.formatFileSize(image.getFileSize()))
                .aspectRatio(calculateAspectRatio(image.getWidth(), image.getHeight()))
                .build();
    }

    /**
     * VideoContent -> ContentDto 변환 (통합 목록용)
     */
    public ContentDto toContentDto(VideoContent video) {
        if (video == null) {
            return null;
        }

        return ContentDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .isAiGenerated(video.getIsAiGenerated())
                .createdDate(video.getCreatedDate())
                .fileSize(video.getFileSize())
                .previewPath(video.getThumbnailPath())
                .contentType("VIDEO")
                .format(video.getVideoFormat())
                .resolution(video.getResolution())
                .formattedFileSize(FileUtils.formatFileSize(video.getFileSize()))
                .duration(video.getDuration())
                .isShorts(video.getIsShorts())
                .build();
    }

    /**
     * ImageContent -> ContentDto 변환 (통합 목록용)
     */
    public ContentDto toContentDto(ImageContent image) {
        if (image == null) {
            return null;
        }

        return ContentDto.builder()
                .id(image.getId())
                .title(image.getTitle())
                .isAiGenerated(image.getIsAiGenerated())
                .createdDate(image.getCreatedDate())
                .fileSize(image.getFileSize())
                .previewPath(image.getFilePath())
                .contentType("IMAGE")
                .format(image.getImageFormat())
                .resolution(image.getResolution())
                .formattedFileSize(FileUtils.formatFileSize(image.getFileSize()))
                .keywords(image.getKeywords())
                .isCompressed(image.getIsCompressed())
                .build();
    }

    /**
     * 시나리오 글자수 제한 (목록 표시용)
     */
    private String truncateScenario(String scenario) {
        if (scenario == null) {
            return null;
        }

        final int MAX_LENGTH = 100; // 100자 제한
        if (scenario.length() <= MAX_LENGTH) {
            return scenario;
        }

        return scenario.substring(0, MAX_LENGTH) + "...";
    }

    /**
     * 영상 길이를 "mm:ss" 또는 "hh:mm:ss" 형식으로 변환
     */
    private String formatDuration(Integer durationInSeconds) {
        if (durationInSeconds == null || durationInSeconds <= 0) {
            return "00:00";
        }

        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 해상도에 따른 화질 라벨 생성
     */
    private String getQualityLabel(Integer width, Integer height) {
        if (width == null || height == null) {
            return "Unknown";
        }

        // 일반적인 해상도 기준
        if (height >= 2160) {
            return "4K";
        } else if (height >= 1440) {
            return "QHD";
        } else if (height >= 1080) {
            return "FHD";
        } else if (height >= 720) {
            return "HD";
        } else if (height >= 480) {
            return "SD";
        } else {
            return "Low";
        }
    }

    /**
     * 종횡비 계산 (16:9, 4:3 등)
     */
    private String calculateAspectRatio(Integer width, Integer height) {
        if (width == null || height == null || height == 0) {
            return "Unknown";
        }

        // 최대공약수를 구해서 비율 계산
        int gcd = gcd(width, height);
        int ratioWidth = width / gcd;
        int ratioHeight = height / gcd;

        // 일반적인 비율들 확인
        if (ratioWidth == 16 && ratioHeight == 9) {
            return "16:9";
        } else if (ratioWidth == 4 && ratioHeight == 3) {
            return "4:3";
        } else if (ratioWidth == 1 && ratioHeight == 1) {
            return "1:1";
        } else if (ratioWidth == 3 && ratioHeight == 2) {
            return "3:2";
        } else {
            return ratioWidth + ":" + ratioHeight;
        }
    }

    /**
     * 최대공약수 계산 (유클리드 호제법)
     */
    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}