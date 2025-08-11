package kt.aivle.content.controller;

import kt.aivle.content.entity.Content;
import kt.aivle.content.entity.ContentType;
import kt.aivle.content.service.ContentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 콘텐츠 공통 API 컨트롤러
 *
 * 이미지와 영상을 통합해서 조회하는 API들을 제공
 */
@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 사용자의 전체 콘텐츠 목록 조회 (페이징)
     * GET /api/contents?page=0&size=20&userId=user123
     */
    @GetMapping
    public ResponseEntity<Page<Content>> getAllContents(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Content> contents = contentService.getContentsByUser(userId, page, size);
        return ResponseEntity.ok(contents);
    }

    /**
     * 특정 타입의 콘텐츠 목록 조회
     * GET /api/contents/type/IMAGE?userId=user123&page=0&size=20
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<Content>> getContentsByType(
            @PathVariable ContentType type,
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Content> contents = contentService.getContentsByUserAndType(userId, type, page, size);
        return ResponseEntity.ok(contents);
    }

    /**
     * 콘텐츠 상세 조회
     * GET /api/contents/123?userId=user123
     */
    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentDetail(
            @PathVariable Long id,
            @RequestParam String userId) {

        return contentService.getContentByIdAndUser(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 제목으로 콘텐츠 검색
     * GET /api/contents/search?userId=user123&title=여행&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Content>> searchContents(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Content> contents = contentService.searchContentsByTitle(userId, title, page, size);
        return ResponseEntity.ok(contents);
    }

    /**
     * 날짜 범위로 콘텐츠 조회
     * GET /api/contents/date-range?userId=user123&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Content>> getContentsByDateRange(
            @RequestParam String userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        List<Content> contents = contentService.getContentsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(contents);
    }

    /**
     * 파일 크기 범위로 콘텐츠 조회
     * GET /api/contents/size-range?userId=user123&minSize=1024&maxSize=10485760&page=0&size=20
     */
    @GetMapping("/size-range")
    public ResponseEntity<Page<Content>> getContentsByFileSize(
            @RequestParam String userId,
            @RequestParam Long minSize,
            @RequestParam Long maxSize,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Content> contents = contentService.getContentsByFileSizeRange(userId, minSize, maxSize, page, size);
        return ResponseEntity.ok(contents);
    }

    /**
     * 사용자 콘텐츠 통계 조회
     * GET /api/contents/stats?userId=user123
     */
    @GetMapping("/stats")
    public ResponseEntity<ContentService.ContentStats> getContentStats(
            @RequestParam String userId) {

        ContentService.ContentStats stats = contentService.getUserContentStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 월별 업로드 통계
     * GET /api/contents/monthly-stats?userId=user123
     */
    @GetMapping("/monthly-stats")
    public ResponseEntity<List<Object[]>> getMonthlyStats(
            @RequestParam String userId) {

        List<Object[]> stats = contentService.getMonthlyUploadStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 콘텐츠 삭제 (Hard Delete)
     * DELETE /api/contents/123?userId=user123
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteContent(
            @PathVariable Long id,
            @RequestParam String userId) {

        try {
            contentService.deleteContent(id, userId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "콘텐츠가 성공적으로 삭제되었습니다."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 헬스 체크 (간단한 상태 확인)
     * GET /api/contents/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Content Management Service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}