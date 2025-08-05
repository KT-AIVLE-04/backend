package kt.aivle.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kt.aivle.content.dto.common.ApiResponse;
import kt.aivle.content.dto.response.FileResourceDto;
import kt.aivle.content.service.VideoStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Stream", description = "영상 스트리밍 API")
public class VideoStreamController {

    private final VideoStreamingService videoStreamingService;

    // 1-5. 영상 재생 (Range Request 지원)
    @GetMapping("/videos/{id}")
    @Operation(summary = "영상 스트리밍", description = "영상을 스트리밍합니다. Range Request를 지원하여 부분 재생이 가능합니다")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable @Parameter(description = "영상 ID") Long id,
            HttpServletRequest request) {

        String rangeHeader = request.getHeader(HttpHeaders.RANGE);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        log.debug("GET /api/stream/videos/{} - Range: {}, User-Agent: {}",
                id, rangeHeader, userAgent);

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            // Range Request 처리 (부분 스트리밍)
            log.debug("Processing range request for video {}: {}", id, rangeHeader);
            return videoStreamingService.streamVideoWithRange(id, rangeHeader);
        } else {
            // 전체 영상 스트리밍
            log.debug("Processing full video stream for video {}", id);
            return videoStreamingService.streamFullVideo(id);
        }
    }

    // 영상 썸네일 조회
    @GetMapping("/videos/{id}/thumbnail")
    @Operation(summary = "영상 썸네일", description = "영상의 썸네일 이미지를 조회합니다 (0:01초 캡처)")
    public ResponseEntity<Resource> getVideoThumbnail(
            @PathVariable @Parameter(description = "영상 ID") Long id) {

        log.debug("GET /api/stream/videos/{}/thumbnail", id);

        FileResourceDto thumbnail = videoStreamingService.getVideoThumbnail(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(thumbnail.getContentType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // 1시간 캐시
                .body(thumbnail.getResource());
    }

    // 영상 스트리밍 정보 조회
    @GetMapping("/videos/{id}/info")
    @Operation(summary = "영상 스트리밍 정보", description = "영상의 스트리밍 관련 메타데이터를 조회합니다")
    public ResponseEntity<ApiResponse<VideoStreamingService.VideoStreamInfoDto>> getVideoStreamInfo(
            @PathVariable @Parameter(description = "영상 ID") Long id) {

        log.debug("GET /api/stream/videos/{}/info", id);

        VideoStreamingService.VideoStreamInfoDto streamInfo = videoStreamingService.getVideoStreamInfo(id);
        return ResponseEntity.ok(ApiResponse.success(streamInfo, "영상 스트리밍 정보 조회 완료"));
    }

    // HLS 매니페스트 조회 (추후 확장용)
    @GetMapping("/videos/{id}/playlist.m3u8")
    @Operation(summary = "HLS 매니페스트", description = "HLS 스트리밍을 위한 m3u8 매니페스트 파일을 조회합니다")
    public ResponseEntity<String> getHLSManifest(
            @PathVariable @Parameter(description = "영상 ID") Long id) {

        log.debug("GET /api/stream/videos/{}/playlist.m3u8", id);

        String manifest = videoStreamingService.generateHLSManifest(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(manifest);
    }

    // 영상 세그먼트 조회 (HLS용 - 추후 확장)
    @GetMapping("/videos/{id}/segments/{segmentName}")
    @Operation(summary = "영상 세그먼트", description = "HLS 스트리밍을 위한 영상 세그먼트를 조회합니다")
    public ResponseEntity<Resource> getVideoSegment(
            @PathVariable @Parameter(description = "영상 ID") Long id,
            @PathVariable @Parameter(description = "세그먼트 파일명") String segmentName) {

        log.debug("GET /api/stream/videos/{}/segments/{}", id, segmentName);

        // 실제 구현에서는 세그먼트 파일을 동적 생성하거나 기존 파일에서 조회
        // 현재는 기본 스트리밍으로 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/api/stream/videos/" + id)
                .build();
    }

    // 영상 품질별 스트리밍 (추후 확장용)
    @GetMapping("/videos/{id}/quality/{quality}")
    @Operation(summary = "품질별 영상 스트리밍", description = "지정된 품질로 영상을 스트리밍합니다")
    public ResponseEntity<Resource> streamVideoByQuality(
            @PathVariable @Parameter(description = "영상 ID") Long id,
            @PathVariable @Parameter(description = "품질 (1080p, 720p, 480p, 360p)") String quality,
            HttpServletRequest request) {

        log.debug("GET /api/stream/videos/{}/quality/{}", id, quality);

        // 실제 구현에서는 품질별로 변환된 영상 파일을 스트리밍
        // 현재는 원본 영상으로 리다이렉트
        String rangeHeader = request.getHeader(HttpHeaders.RANGE);

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            return videoStreamingService.streamVideoWithRange(id, rangeHeader);
        } else {
            return videoStreamingService.streamFullVideo(id);
        }
    }

    // 영상 미리보기 (짧은 클립 - 추후 확장용)
    @GetMapping("/videos/{id}/preview")
    @Operation(summary = "영상 미리보기", description = "영상의 미리보기 클립을 스트리밍합니다")
    public ResponseEntity<Resource> getVideoPreview(
            @PathVariable @Parameter(description = "영상 ID") Long id,
            @RequestParam(defaultValue = "30") @Parameter(description = "미리보기 길이(초)") int duration) {

        log.debug("GET /api/stream/videos/{}/preview - duration: {}s", id, duration);

        // 실제 구현에서는 미리보기용 짧은 클립을 생성하여 스트리밍
        // 현재는 원본 영상으로 리다이렉트
        return videoStreamingService.streamFullVideo(id);
    }

    // CORS 프리플라이트 요청 처리
    @RequestMapping(value = "/videos/**", method = RequestMethod.OPTIONS)
    @Operation(summary = "CORS 프리플라이트", description = "CORS 프리플라이트 요청을 처리합니다")
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, HEAD, OPTIONS")
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Range, Content-Type")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Range, Content-Length")
                .build();
    }

    // 영상 상태 확인 (헬스체크)
    @GetMapping("/videos/{id}/status")
    @Operation(summary = "영상 상태 확인", description = "영상 파일의 상태를 확인합니다")
    public ResponseEntity<ApiResponse<VideoStatusDto>> checkVideoStatus(
            @PathVariable @Parameter(description = "영상 ID") Long id) {

        log.debug("GET /api/stream/videos/{}/status", id);

        try {
            // 영상 스트리밍 정보 조회로 파일 존재 여부 확인
            VideoStreamingService.VideoStreamInfoDto streamInfo = videoStreamingService.getVideoStreamInfo(id);

            VideoStatusDto status = VideoStatusDto.builder()
                    .id(id)
                    .available(true)
                    .fileSize(streamInfo.getFileSize())
                    .duration(streamInfo.getDuration())
                    .format(streamInfo.getFormat())
                    .message("영상이 정상적으로 스트리밍 가능합니다")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(status, "영상 상태 확인 완료"));

        } catch (Exception e) {
            VideoStatusDto status = VideoStatusDto.builder()
                    .id(id)
                    .available(false)
                    .message("영상 파일에 문제가 있습니다: " + e.getMessage())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(status, "영상 상태 확인 완료"));
        }
    }

    /**
     * 영상 상태 DTO
     */
    public static class VideoStatusDto {
        private Long id;
        private Boolean available;
        private Long fileSize;
        private Integer duration;
        private String format;
        private String message;

        public static VideoStatusDtoBuilder builder() {
            return new VideoStatusDtoBuilder();
        }

        public static class VideoStatusDtoBuilder {
            private VideoStatusDto dto = new VideoStatusDto();

            public VideoStatusDtoBuilder id(Long id) { dto.id = id; return this; }
            public VideoStatusDtoBuilder available(Boolean available) { dto.available = available; return this; }
            public VideoStatusDtoBuilder fileSize(Long fileSize) { dto.fileSize = fileSize; return this; }
            public VideoStatusDtoBuilder duration(Integer duration) { dto.duration = duration; return this; }
            public VideoStatusDtoBuilder format(String format) { dto.format = format; return this; }
            public VideoStatusDtoBuilder message(String message) { dto.message = message; return this; }

            public VideoStatusDto build() { return dto; }
        }

        // Getters
        public Long getId() { return id; }
        public Boolean getAvailable() { return available; }
        public Long getFileSize() { return fileSize; }
        public Integer getDuration() { return duration; }
        public String getFormat() { return format; }
        public String getMessage() { return message; }
    }
}