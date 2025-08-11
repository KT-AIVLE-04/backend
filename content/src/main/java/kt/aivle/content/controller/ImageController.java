package kt.aivle.content.controller;

import kt.aivle.content.entity.Image;
import kt.aivle.content.service.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 이미지 전용 API 컨트롤러
 *
 * 이미지 업로드, 조회, 관리 등의 기능을 제공
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * 이미지 업로드
     * POST /api/images/upload
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "title", required = false) String title) {

        try {
            Image uploadedImage = imageService.uploadImage(file, title, userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "이미지가 성공적으로 업로드되었습니다.",
                    "data", Map.of(
                            "id", uploadedImage.getId(),
                            "title", uploadedImage.getTitle(),
                            "url", uploadedImage.getS3Url(),
                            "thumbnailUrl", uploadedImage.getThumbnailUrl(),
                            "fileSize", uploadedImage.getFileSize(),
                            "width", uploadedImage.getWidth(),
                            "height", uploadedImage.getHeight(),
                            "createdAt", uploadedImage.getCreatedAt()
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
     * 사용자별 이미지 목록 조회 (페이징)
     * GET /api/images?userId=user123&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<Image>> getImages(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Image> images = imageService.getImagesByUser(userId, page, size);
        return ResponseEntity.ok(images);
    }

    /**
     * 이미지 상세 조회
     * GET /api/images/123?userId=user123
     */
    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageDetail(
            @PathVariable Long id,
            @RequestParam String userId) {

        return imageService.getImageByIdAndUser(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 해상도별 이미지 조회
     * GET /api/images/resolution?userId=user123&minWidth=1920&minHeight=1080&page=0&size=20
     */
    @GetMapping("/resolution")
    public ResponseEntity<Page<Image>> getImagesByResolution(
            @RequestParam String userId,
            @RequestParam Integer minWidth,
            @RequestParam Integer minHeight,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Image> images = imageService.getImagesByResolution(userId, minWidth, minHeight, page, size);
        return ResponseEntity.ok(images);
    }

    /**
     * 가로세로 비율별 이미지 조회
     * GET /api/images/aspect-ratio?userId=user123&aspectRatio=landscape&page=0&size=20
     * aspectRatio: landscape, portrait, square
     */
    @GetMapping("/aspect-ratio")
    public ResponseEntity<Page<Image>> getImagesByAspectRatio(
            @RequestParam String userId,
            @RequestParam String aspectRatio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Image> images = imageService.getImagesByAspectRatio(userId, aspectRatio, page, size);
        return ResponseEntity.ok(images);
    }

    /**
     * 파일명으로 이미지 검색
     * GET /api/images/search?userId=user123&filename=vacation&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Image>> searchImages(
            @RequestParam String userId,
            @RequestParam String filename,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Image> images = imageService.searchImagesByFilename(userId, filename, page, size);
        return ResponseEntity.ok(images);
    }

    /**
     * 최근 업로드된 이미지 조회 (미리보기용)
     * GET /api/images/recent?userId=user123&limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Image>> getRecentImages(
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<Image> images = imageService.getRecentImages(userId, limit);
        return ResponseEntity.ok(images);
    }

    /**
     * 해상도별 통계
     * GET /api/images/stats/resolution?userId=user123
     */
    @GetMapping("/stats/resolution")
    public ResponseEntity<List<Object[]>> getResolutionStats(
            @RequestParam String userId) {

        List<Object[]> stats = imageService.getResolutionStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 이미지 삭제
     * DELETE /api/images/123?userId=user123
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteImage(
            @PathVariable Long id,
            @RequestParam String userId) {

        try {
            imageService.deleteImage(id, userId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "이미지가 성공적으로 삭제되었습니다."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 다중 이미지 업로드
     * POST /api/images/upload/multiple
     */
    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("userId") String userId,
            @RequestParam(value = "titles", required = false) String[] titles) {

        try {
            List<Image> uploadedImages = new java.util.ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                String title = (titles != null && i < titles.length) ? titles[i] : null;
                Image uploadedImage = imageService.uploadImage(files[i], title, userId);
                uploadedImages.add(uploadedImage);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", files.length + "개의 이미지가 성공적으로 업로드되었습니다.",
                    "data", uploadedImages.stream().map(image -> Map.of(
                            "id", image.getId(),
                            "title", image.getTitle(),
                            "url", image.getS3Url(),
                            "thumbnailUrl", image.getThumbnailUrl(),
                            "createdAt", image.getCreatedAt()
                    )).toList()
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
     * POST /api/images/thumbnails/regenerate
     */
    @PostMapping("/thumbnails/regenerate")
    public ResponseEntity<Map<String, String>> regenerateThumbnails() {
        try {
            imageService.generateMissingThumbnails();
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