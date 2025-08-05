package kt.aivle.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kt.aivle.content.dto.common.ApiResponse;
import kt.aivle.content.dto.common.ContentDto;
import kt.aivle.content.dto.request.ContentSearchDto;
import kt.aivle.content.dto.request.ImageUploadDto;
import kt.aivle.content.dto.request.VideoUploadDto;
import kt.aivle.content.dto.response.*;
import kt.aivle.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Content", description = "콘텐츠 관리 API")
public class ContentController {

    private final ContentService contentService;

    // 1-1. 영상 생성 이력/목록 조회
    @GetMapping("/videos")
    @Operation(summary = "영상 목록 조회", description = "영상 생성 이력 및 목록을 페이지네이션으로 조회합니다")
    public ResponseEntity<ApiResponse<Page<VideoContentDto>>> getVideoHistory(
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC)
            @Parameter(description = "페이지네이션 정보") Pageable pageable) {

        log.debug("GET /api/contents/videos - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<VideoContentDto> videos = contentService.getVideoHistory(pageable);
        return ResponseEntity.ok(ApiResponse.success(videos, "영상 목록 조회 완료"));
    }

    // 1-1. 이미지 생성 이력/목록 조회
    @GetMapping("/images")
    @Operation(summary = "이미지 목록 조회", description = "이미지 생성 이력 및 목록을 페이지네이션으로 조회합니다")
    public ResponseEntity<ApiResponse<Page<ImageContentDto>>> getImageHistory(
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC)
            @Parameter(description = "페이지네이션 정보") Pageable pageable) {

        log.debug("GET /api/contents/images - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<ImageContentDto> images = contentService.getImageHistory(pageable);
        return ResponseEntity.ok(ApiResponse.success(images, "이미지 목록 조회 완료"));
    }

    // 1-2. 전체/필터별 조회
    @GetMapping
    @Operation(summary = "전체 콘텐츠 조회", description = "전체 콘텐츠를 필터와 함께 조회합니다")
    public ResponseEntity<ApiResponse<Page<ContentDto>>> getAllContents(
            @ModelAttribute @Valid @Parameter(description = "검색 필터") ContentSearchDto searchDto,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC)
            @Parameter(description = "페이지네이션 정보") Pageable pageable) {

        log.debug("GET /api/contents - filter: {}, page: {}", searchDto, pageable.getPageNumber());

        Page<ContentDto> contents = contentService.getAllContents(searchDto, pageable);
        return ResponseEntity.ok(ApiResponse.success(contents, "전체 콘텐츠 조회 완료"));
    }

    // 1-3. 제목 기반 검색
    @GetMapping("/search")
    @Operation(summary = "콘텐츠 검색", description = "제목을 기반으로 영상과 이미지를 통합 검색합니다")
    public ResponseEntity<ApiResponse<Page<ContentDto>>> searchContents(
            @RequestParam @NotNull @Parameter(description = "검색할 제목") String title,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC)
            @Parameter(description = "페이지네이션 정보") Pageable pageable) {

        log.debug("GET /api/contents/search - title: {}, page: {}", title, pageable.getPageNumber());

        Page<ContentDto> contents = contentService.searchContentsByTitle(title, pageable);
        return ResponseEntity.ok(ApiResponse.success(contents, "검색 완료"));
    }

    // 1-4. 영상 상세 조회
    @GetMapping("/videos/{id}")
    @Operation(summary = "영상 상세 조회", description = "영상의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<VideoContentDetailDto>> getVideoDetail(
            @PathVariable @Parameter(description = "영상 ID") Long id) {

        log.debug("GET /api/contents/videos/{}", id);

        VideoContentDetailDto video = contentService.getVideoDetail(id);
        return ResponseEntity.ok(ApiResponse.success(video, "영상 상세 조회 완료"));
    }

    // 1-4. 이미지 상세 조회
    @GetMapping("/images/{id}")
    @Operation(summary = "이미지 상세 조회", description = "이미지의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<ImageContentDetailDto>> getImageDetail(
            @PathVariable @Parameter(description = "이미지 ID") Long id) {

        log.debug("GET /api/contents/images/{}", id);

        ImageContentDetailDto image = contentService.getImageDetail(id);
        return ResponseEntity.ok(ApiResponse.success(image, "이미지 상세 조회 완료"));
    }

    // 2-1. 영상 업로드
    @PostMapping(value = "/videos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "영상 업로드", description = "영상 파일을 업로드합니다 (MP4, MOV, AVI, WMV 지원)")
    public ResponseEntity<ApiResponse<VideoContentDto>> uploadVideo(
            @RequestParam("file") @Parameter(description = "업로드할 영상 파일") MultipartFile file,
            @ModelAttribute @Valid @Parameter(description = "영상 정보") VideoUploadDto uploadDto) {

        log.info("POST /api/contents/videos/upload - file: {}, title: {}",
                file.getOriginalFilename(), uploadDto.getTitle());

        VideoContentDto video = contentService.uploadVideo(file, uploadDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(video, "영상 업로드 완료"));
    }

    // 2-1. 이미지 업로드
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드합니다 (JPG, PNG, WebP 지원)")
    public ResponseEntity<ApiResponse<ImageContentDto>> uploadImage(
            @RequestParam("file") @Parameter(description = "업로드할 이미지 파일") MultipartFile file,
            @ModelAttribute @Valid @Parameter(description = "이미지 정보") ImageUploadDto uploadDto) {

        log.info("POST /api/contents/images/upload - file: {}, title: {}",
                file.getOriginalFilename(), uploadDto.getTitle());

        ImageContentDto image = contentService.uploadImage(file, uploadDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(image, "이미지 업로드 완료"));
    }

    // 3. 영상 삭제
    @DeleteMapping("/videos/{id}")
    @Operation(summary = "영상 삭제", description = "영상을 삭제합니다 (소프트 삭제 또는 하드 삭제)")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(
            @PathVariable @Parameter(description = "영상 ID") Long id,
            @RequestParam(defaultValue = "false") @Parameter(description = "하드 삭제 여부") boolean hardDelete) {

        log.info("DELETE /api/contents/videos/{} - hardDelete: {}", id, hardDelete);

        contentService.deleteVideo(id, hardDelete);
        return ResponseEntity.ok(ApiResponse.success(null, "영상 삭제 완료"));
    }

    // 3. 이미지 삭제
    @DeleteMapping("/images/{id}")
    @Operation(summary = "이미지 삭제", description = "이미지를 삭제합니다 (소프트 삭제 또는 하드 삭제)")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable @Parameter(description = "이미지 ID") Long id,
            @RequestParam(defaultValue = "false") @Parameter(description = "하드 삭제 여부") boolean hardDelete) {

        log.info("DELETE /api/contents/images/{} - hardDelete: {}", id, hardDelete);

        contentService.deleteImage(id, hardDelete);
        return ResponseEntity.ok(ApiResponse.success(null, "이미지 삭제 완료"));
    }

    // 4. 영상 다운로드
    @GetMapping("/videos/{id}/download")
    @Operation(summary = "영상 다운로드", description = "영상 파일을 다운로드합니다")
    public ResponseEntity<Resource> downloadVideo(
            @PathVariable @Parameter(description = "영상 ID") Long id) {

        log.debug("GET /api/contents/videos/{}/download", id);

        FileResourceDto fileResource = contentService.prepareVideoDownload(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileResource.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileResource.getFileSize()))
                .body(fileResource.getResource());
    }

    // 4. 이미지 다운로드
    @GetMapping("/images/{id}/download")
    @Operation(summary = "이미지 다운로드", description = "이미지 파일을 다운로드합니다")
    public ResponseEntity<Resource> downloadImage(
            @PathVariable @Parameter(description = "이미지 ID") Long id) {

        log.debug("GET /api/contents/images/{}/download", id);

        FileResourceDto fileResource = contentService.prepareImageDownload(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileResource.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileResource.getFileSize()))
                .body(fileResource.getResource());
    }

    // 추가 기능: 통계 조회
    @GetMapping("/statistics")
    @Operation(summary = "콘텐츠 통계", description = "콘텐츠 업로드 통계를 조회합니다")
    public ResponseEntity<ApiResponse<ContentStatsDto>> getStatistics() {

        log.debug("GET /api/contents/statistics");

        ContentStatsDto stats = contentService.getContentStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "통계 조회 완료"));
    }

    // 추가 기능: 최근 콘텐츠 조회
    @GetMapping("/recent")
    @Operation(summary = "최근 콘텐츠", description = "최근 업로드된 콘텐츠를 조회합니다")
    public ResponseEntity<ApiResponse<Page<ContentDto>>> getRecentContents(
            @RequestParam(defaultValue = "10") @Parameter(description = "조회할 개수") int size) {

        log.debug("GET /api/contents/recent - size: {}", size);

        Page<ContentDto> recentContents = contentService.getRecentContents(size);
        return ResponseEntity.ok(ApiResponse.success(recentContents, "최근 콘텐츠 조회 완료"));
    }

    // 추가 기능: 콘텐츠 미리보기 (썸네일/이미지)
    @GetMapping("/preview/{id}")
    @Operation(summary = "콘텐츠 미리보기", description = "콘텐츠의 미리보기 이미지를 조회합니다")
    public ResponseEntity<Resource> getContentPreview(
            @PathVariable @Parameter(description = "콘텐츠 ID") Long id,
            @RequestParam @Parameter(description = "콘텐츠 타입 (video/image)") String type) {

        log.debug("GET /api/contents/preview/{} - type: {}", id, type);

        FileResourceDto preview;
        if ("video".equalsIgnoreCase(type)) {
            // 영상의 경우 썸네일 반환 (VideoStreamController에서 처리하는 것이 더 적절)
            throw new UnsupportedOperationException("영상 썸네일은 /api/stream/videos/{id}/thumbnail을 사용하세요");
        } else {
            // 이미지의 경우 원본 또는 압축된 이미지 반환
            preview = contentService.prepareImageDownload(id);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(preview.getContentType()))
                .body(preview.getResource());
    }
}