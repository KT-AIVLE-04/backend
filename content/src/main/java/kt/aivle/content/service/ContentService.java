package kt.aivle.content.service;

import kt.aivle.content.dto.common.ContentDto;
import kt.aivle.content.dto.request.ContentSearchDto;
import kt.aivle.content.dto.request.ImageUploadDto;
import kt.aivle.content.dto.request.VideoUploadDto;
import kt.aivle.content.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ContentService {

    // 1-1. 생성 이력 및 목록 조회
    /**
     * 영상 생성 이력/목록 조회 (페이지네이션)
     */
    Page<VideoContentDto> getVideoHistory(Pageable pageable);

    /**
     * 이미지 생성 이력/목록 조회 (페이지네이션)
     */
    Page<ImageContentDto> getImageHistory(Pageable pageable);

    // 1-2. 전체/필터별 조회
    /**
     * 전체 콘텐츠 조회 (페이지네이션, 필터)
     */
    Page<ContentDto> getAllContents(ContentSearchDto searchDto, Pageable pageable);

    // 1-3. 검색
    /**
     * 제목 기반 기본 검색 (영상+이미지)
     */
    Page<ContentDto> searchContentsByTitle(String title, Pageable pageable);

    // 1-4. 상세 조회
    /**
     * 영상 상세 조회
     */
    VideoContentDetailDto getVideoDetail(Long id);

    /**
     * 이미지 상세 조회
     */
    ImageContentDetailDto getImageDetail(Long id);

    // 2-1. 사용자 업로드
    /**
     * 영상 업로드
     */
    VideoContentDto uploadVideo(MultipartFile file, VideoUploadDto uploadDto);

    /**
     * 이미지 업로드
     */
    ImageContentDto uploadImage(MultipartFile file, ImageUploadDto uploadDto);

    // 3. 콘텐츠 삭제
    /**
     * 영상 삭제 (Hard/Soft 삭제 선택)
     */
    void deleteVideo(Long id, boolean hardDelete);

    /**
     * 이미지 삭제 (Hard/Soft 삭제 선택)
     */
    void deleteImage(Long id, boolean hardDelete);

    // 4. 콘텐츠 다운로드 준비
    /**
     * 영상 다운로드를 위한 파일 리소스 준비
     */
    FileResourceDto prepareVideoDownload(Long id);

    /**
     * 이미지 다운로드를 위한 파일 리소스 준비
     */
    FileResourceDto prepareImageDownload(Long id);

    // 추가 기능들
    /**
     * 콘텐츠 통계 조회
     */
    ContentStatsDto getContentStatistics();

    /**
     * 최근 업로드된 콘텐츠 조회
     */
    Page<ContentDto> getRecentContents(int size);

    /**
     * 사용자별 콘텐츠 조회 (추후 사용자 기능 추가 시)
     */
    Page<ContentDto> getContentsByUser(String userId, Pageable pageable);
}