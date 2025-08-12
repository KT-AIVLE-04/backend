package kt.aivle.content.controller;

import kt.aivle.content.entity.Video;
import kt.aivle.content.service.VideoService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 영상 전용 API 컨트롤러
 *
 * 영상 업로드, 조회, 관리, 압축 등의 기능을 제공
 * 숏츠와 일반 영상을 구분해서 처리
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * 영상 업로드 (썸네일 포함)
     * POST /api/videos/upload
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile) {

        try {
            Video uploadedVideo = videoService.uploadVideo(file, title, userId, thumbnailFile);

            // 응답 데이터 구성 - null 값 처리
            Map<String, Object> responseData = new java.util.LinkedHashMap<>();
            responseData.put("id", uploadedVideo.getId());
            responseData.put("title", uploadedVideo.getTitle());
            responseData.put("url", uploadedVideo.getS3Url());
            responseData.put("thumbnailUrl", uploadedVideo.getThumbnailUrl() != null ? uploadedVideo.getThumbnailUrl() : "");
            responseData.put("fileSize", uploadedVideo.getFileSize());
            responseData.put("width", uploadedVideo.getWidth() != null ? uploadedVideo.getWidth() : 0);
            responseData.put("height", uploadedVideo.getHeight() != null ? uploadedVideo.getHeight() : 0);
            responseData.put("duration", uploadedVideo.getDurationSeconds() != null ? uploadedVideo.getDurationSeconds() : 0);
            responseData.put("formattedDuration", uploadedVideo.getFormattedDuration());
            responseData.put("isShort", uploadedVideo.getIsShort() != null ? uploadedVideo.getIsShort() : false);
            responseData.put("codec", uploadedVideo.getCodec() != null ? uploadedVideo.getCodec() : "");
            responseData.put("bitrate", uploadedVideo.getBitrate() != null ? uploadedVideo.getBitrate() : 0);
            responseData.put("frameRate", uploadedVideo.getFrameRate() != null ? uploadedVideo.getFrameRate() : 0.0);
            responseData.put("createdAt", uploadedVideo.getCreatedAt());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "영상이 성공적으로 업로드되었습니다.",
                    "data", responseData
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 사용자별 영상 목록 조회 (페이징)
     * GET /api/videos?userId=user123&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<Video>> getVideos(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.getVideosByUser(userId, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 숏츠 영상만 조회
     * GET /api/videos/shorts?userId=user123&page=0&size=20
     */
    @GetMapping("/shorts")
    public ResponseEntity<Page<Video>> getShorts(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> shorts = videoService.getShortsByUser(userId, page, size);
        return ResponseEntity.ok(shorts);
    }

    /**
     * 일반 영상만 조회 (숏츠 제외)
     * GET /api/videos/regular?userId=user123&page=0&size=20
     */
    @GetMapping("/regular")
    public ResponseEntity<Page<Video>> getRegularVideos(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.getRegularVideosByUser(userId, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 영상 상세 조회
     * GET /api/videos/123?userId=user123
     */
    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoDetail(
            @PathVariable Long id,
            @RequestParam String userId) {

        return videoService.getVideoByIdAndUser(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 영상 길이별 조회
     * GET /api/videos/duration?userId=user123&minDuration=60&maxDuration=3600&page=0&size=20
     */
    @GetMapping("/duration")
    public ResponseEntity<Page<Video>> getVideosByDuration(
            @RequestParam String userId,
            @RequestParam Integer minDuration,
            @RequestParam Integer maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.getVideosByDuration(userId, minDuration, maxDuration, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 해상도별 영상 조회
     * GET /api/videos/resolution?userId=user123&minWidth=1920&minHeight=1080&page=0&size=20
     */
    @GetMapping("/resolution")
    public ResponseEntity<Page<Video>> getVideosByResolution(
            @RequestParam String userId,
            @RequestParam Integer minWidth,
            @RequestParam Integer minHeight,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.getVideosByResolution(userId, minWidth, minHeight, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 코덱별 영상 조회
     * GET /api/videos/codec?userId=user123&codec=h264&page=0&size=20
     */
    @GetMapping("/codec")
    public ResponseEntity<Page<Video>> getVideosByCodec(
            @RequestParam String userId,
            @RequestParam String codec,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.getVideosByCodec(userId, codec, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 비트레이트별 영상 조회
     * GET /api/videos/bitrate?userId=user123&minBitrate=1000&maxBitrate=10000&page=0&size=20
     */
    @GetMapping("/bitrate")
    public ResponseEntity<Page<Video>> getVideosByBitrate(
            @RequestParam String userId,
            @RequestParam Integer minBitrate,
            @RequestParam Integer maxBitrate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.getVideosByBitrate(userId, minBitrate, maxBitrate, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 파일명으로 영상 검색
     * GET /api/videos/search?userId=user123&filename=meeting&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Video>> searchVideos(
            @RequestParam String userId,
            @RequestParam String filename,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Video> videos = videoService.searchVideosByFilename(userId, filename, page, size);
        return ResponseEntity.ok(videos);
    }

    /**
     * 최근 업로드된 영상 조회 (미리보기용)
     * GET /api/videos/recent?userId=user123&limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Video>> getRecentVideos(
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<Video> videos = videoService.getRecentVideos(userId, limit);
        return ResponseEntity.ok(videos);
    }

    /**
     * 해상도별 통계
     * GET /api/videos/stats/resolution?userId=user123
     */
    @GetMapping("/stats/resolution")
    public ResponseEntity<List<Object[]>> getResolutionStats(
            @RequestParam String userId) {

        List<Object[]> stats = videoService.getResolutionStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 영상 길이별 통계
     * GET /api/videos/stats/duration?userId=user123
     */
    @GetMapping("/stats/duration")
    public ResponseEntity<List<Object[]>> getDurationStats(
            @RequestParam String userId) {

        List<Object[]> stats = videoService.getDurationStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 총 영상 재생 시간 조회
     * GET /api/videos/stats/total-duration?userId=user123
     */
    @GetMapping("/stats/total-duration")
    public ResponseEntity<Map<String, Object>> getTotalDuration(
            @RequestParam String userId) {

        Long totalSeconds = videoService.getTotalDuration(userId);
        String formattedDuration = videoService.getFormattedTotalDuration(userId);

        return ResponseEntity.ok(Map.of(
                "totalSeconds", totalSeconds != null ? totalSeconds : 0,
                "formattedDuration", formattedDuration
        ));
    }

    /**
     * 영상 삭제
     * DELETE /api/videos/123?userId=user123
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVideo(
            @PathVariable Long id,
            @RequestParam String userId) {

        try {
            videoService.deleteVideo(id, userId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "영상이 성공적으로 삭제되었습니다."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 영상 압축
     * POST /api/videos/123/compress?userId=user123&quality=medium
     */
    @PostMapping("/{id}/compress")
    public ResponseEntity<Map<String, Object>> compressVideo(
            @PathVariable Long id,
            @RequestParam String userId,
            @RequestParam(defaultValue = "medium") String quality) {

        try {
            Video compressedVideo = videoService.compressVideo(id, userId, quality);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "영상 압축이 시작되었습니다.",
                    "data", Map.of(
                            "id", compressedVideo.getId(),
                            "title", compressedVideo.getTitle(),
                            "url", compressedVideo.getS3Url()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 썸네일 재생성 (관리자용)
     * POST /api/videos/thumbnails/regenerate
     */
    @PostMapping("/thumbnails/regenerate")
    public ResponseEntity<Map<String, String>> regenerateThumbnails() {
        try {
            videoService.generateMissingThumbnails();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "썸네일 재생성 작업이 시작되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}