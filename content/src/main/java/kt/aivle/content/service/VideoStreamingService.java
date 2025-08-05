package kt.aivle.content.service;

import kt.aivle.content.domain.VideoContent;
import kt.aivle.content.dto.response.FileResourceDto;
import kt.aivle.content.exception.ContentNotFoundException;
import kt.aivle.content.exception.FileProcessingException;
import kt.aivle.content.repository.VideoContentRepository;
import kt.aivle.content.resource.PartialFileSystemResource;
import kt.aivle.content.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoStreamingService {

    private final VideoContentRepository videoRepository;

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunks

    /**
     * Range Request를 지원하는 영상 스트리밍
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> streamVideoWithRange(Long videoId, String rangeHeader) {
        log.debug("Streaming video with range - ID: {}, Range: {}", videoId, rangeHeader);

        VideoContent video = getVideoContent(videoId);

        try {
            Path videoPath = Paths.get(video.getFilePath());
            long fileSize = Files.size(videoPath);

            // Range 헤더 파싱
            RangeInfo rangeInfo = parseRangeHeader(rangeHeader, fileSize);

            // 부분 콘텐츠 리소스 생성
            Resource resource = new PartialFileSystemResource(videoPath, rangeInfo.getStart(), rangeInfo.getEnd());

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, getVideoContentType(video.getVideoFormat()))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeInfo.getContentLength()))
                    .header(HttpHeaders.CONTENT_RANGE,
                            String.format("bytes %d-%d/%d",
                                    rangeInfo.getStart(),
                                    rangeInfo.getEnd(),
                                    fileSize))
                    .body(resource);

        } catch (IOException e) {
            log.error("Error streaming video with range: {}", e.getMessage());
            throw new FileProcessingException("영상 스트리밍에 실패했습니다", e);
        }
    }

    /**
     * 전체 영상 스트리밍
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> streamFullVideo(Long videoId) {
        log.debug("Streaming full video - ID: {}", videoId);

        VideoContent video = getVideoContent(videoId);

        try {
            Path videoPath = Paths.get(video.getFilePath());
            Resource resource = new FileSystemResource(videoPath);

            if (!resource.exists()) {
                throw new ContentNotFoundException("영상 파일이 존재하지 않습니다");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getVideoContentType(video.getVideoFormat()))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(video.getFileSize()))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error streaming full video: {}", e.getMessage());
            throw new FileProcessingException("영상 스트리밍에 실패했습니다", e);
        }
    }

    /**
     * 영상 썸네일 조회
     */
    @Transactional(readOnly = true)
    public FileResourceDto getVideoThumbnail(Long videoId) {
        log.debug("Getting video thumbnail - ID: {}", videoId);

        VideoContent video = getVideoContent(videoId);

        if (video.getThumbnailPath() == null) {
            throw new ContentNotFoundException("썸네일이 존재하지 않습니다");
        }

        try {
            Path thumbnailPath = Paths.get(video.getThumbnailPath());
            Resource resource = new FileSystemResource(thumbnailPath);

            if (!resource.exists()) {
                throw new ContentNotFoundException("썸네일 파일이 존재하지 않습니다");
            }

            return FileResourceDto.builder()
                    .resource(resource)
                    .fileName("thumbnail_" + video.getId() + ".jpg")
                    .contentType("image/jpeg")
                    .fileSize(Files.size(thumbnailPath))
                    .build();

        } catch (IOException e) {
            log.error("Error loading thumbnail: {}", e.getMessage());
            throw new FileProcessingException("썸네일 로딩에 실패했습니다", e);
        }
    }

    /**
     * 영상 메타데이터 조회 (스트리밍 정보용)
     */
    @Transactional(readOnly = true)
    public VideoStreamInfoDto getVideoStreamInfo(Long videoId) {
        log.debug("Getting video stream info - ID: {}", videoId);

        VideoContent video = getVideoContent(videoId);

        return VideoStreamInfoDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .duration(video.getDuration())
                .width(video.getWidth())
                .height(video.getHeight())
                .bitrate(video.getBitrate())
                .frameRate(video.getFrameRate())
                .fileSize(video.getFileSize())
                .format(video.getVideoFormat())
                .thumbnailUrl("/api/stream/videos/" + video.getId() + "/thumbnail")
                .streamUrl("/api/stream/videos/" + video.getId())
                .supportsRangeRequests(true)
                .build();
    }

    /**
     * HLS 매니페스트 생성 (추후 확장용)
     */
    public String generateHLSManifest(Long videoId) {
        VideoContent video = getVideoContent(videoId);

        // HLS m3u8 매니페스트 생성 로직
        // 실제 구현 시 FFmpeg을 사용하여 세그먼트 생성
        StringBuilder manifest = new StringBuilder();
        manifest.append("#EXTM3U\n");
        manifest.append("#EXT-X-VERSION:3\n");
        manifest.append("#EXT-X-TARGETDURATION:10\n");
        manifest.append("#EXT-X-MEDIA-SEQUENCE:0\n");

        // 세그먼트 정보 추가 (실제로는 동적 생성)
        int segments = video.getDuration() / 10; // 10초 세그먼트
        for (int i = 0; i < segments; i++) {
            manifest.append("#EXTINF:10.0,\n");
            manifest.append(String.format("segment_%d.ts\n", i));
        }

        manifest.append("#EXT-X-ENDLIST\n");
        return manifest.toString();
    }

    private VideoContent getVideoContent(Long videoId) {
        return videoRepository.findByIdAndIsDeletedFalse(videoId)
                .orElseThrow(() -> new ContentNotFoundException("영상을 찾을 수 없습니다. ID: " + videoId));
    }

    private RangeInfo parseRangeHeader(String rangeHeader, long fileSize) {
        // "bytes=start-end" 형식 파싱
        String range = rangeHeader.substring("bytes=".length());
        String[] ranges = range.split("-");

        long start = 0;
        long end = fileSize - 1;

        if (ranges.length == 1) {
            if (range.startsWith("-")) {
                // "-500" (마지막 500바이트)
                start = Math.max(0, fileSize - Long.parseLong(ranges[0].substring(1)));
            } else {
                // "500-" (500부터 끝까지)
                start = Long.parseLong(ranges[0]);
            }
        } else if (ranges.length == 2) {
            // "500-1000"
            start = Long.parseLong(ranges[0]);
            if (!ranges[1].isEmpty()) {
                end = Math.min(Long.parseLong(ranges[1]), fileSize - 1);
            }
        }

        return new RangeInfo(start, end, end - start + 1);
    }

    private String getVideoContentType(String format) {
        return switch (format.toUpperCase()) {
            case "MP4" -> "video/mp4";
            case "MOV" -> "video/quicktime";
            case "AVI" -> "video/x-msvideo";
            case "WMV" -> "video/x-ms-wmv";
            default -> "application/octet-stream";
        };
    }

    /**
     * Range 정보를 담는 내부 클래스
     */
    public static class RangeInfo {
        private final long start;
        private final long end;
        private final long contentLength;

        public RangeInfo(long start, long end, long contentLength) {
            this.start = start;
            this.end = end;
            this.contentLength = contentLength;
        }

        public long getStart() { return start; }
        public long getEnd() { return end; }
        public long getContentLength() { return contentLength; }
    }

    /**
     * 영상 스트리밍 정보 DTO
     */
    public static class VideoStreamInfoDto {
        private Long id;
        private String title;
        private Integer duration;
        private Integer width;
        private Integer height;
        private Integer bitrate;
        private Double frameRate;
        private Long fileSize;
        private String format;
        private String thumbnailUrl;
        private String streamUrl;
        private Boolean supportsRangeRequests;

        // Builder pattern
        public static VideoStreamInfoDtoBuilder builder() {
            return new VideoStreamInfoDtoBuilder();
        }

        public static class VideoStreamInfoDtoBuilder {
            private VideoStreamInfoDto dto = new VideoStreamInfoDto();

            public VideoStreamInfoDtoBuilder id(Long id) { dto.id = id; return this; }
            public VideoStreamInfoDtoBuilder title(String title) { dto.title = title; return this; }
            public VideoStreamInfoDtoBuilder duration(Integer duration) { dto.duration = duration; return this; }
            public VideoStreamInfoDtoBuilder width(Integer width) { dto.width = width; return this; }
            public VideoStreamInfoDtoBuilder height(Integer height) { dto.height = height; return this; }
            public VideoStreamInfoDtoBuilder bitrate(Integer bitrate) { dto.bitrate = bitrate; return this; }
            public VideoStreamInfoDtoBuilder frameRate(Double frameRate) { dto.frameRate = frameRate; return this; }
            public VideoStreamInfoDtoBuilder fileSize(Long fileSize) { dto.fileSize = fileSize; return this; }
            public VideoStreamInfoDtoBuilder format(String format) { dto.format = format; return this; }
            public VideoStreamInfoDtoBuilder thumbnailUrl(String thumbnailUrl) { dto.thumbnailUrl = thumbnailUrl; return this; }
            public VideoStreamInfoDtoBuilder streamUrl(String streamUrl) { dto.streamUrl = streamUrl; return this; }
            public VideoStreamInfoDtoBuilder supportsRangeRequests(Boolean supportsRangeRequests) { dto.supportsRangeRequests = supportsRangeRequests; return this; }

            public VideoStreamInfoDto build() { return dto; }
        }

        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public Integer getDuration() { return duration; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public Integer getBitrate() { return bitrate; }
        public Double getFrameRate() { return frameRate; }
        public Long getFileSize() { return fileSize; }
        public String getFormat() { return format; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getStreamUrl() { return streamUrl; }
        public Boolean getSupportsRangeRequests() { return supportsRangeRequests; }
    }
}