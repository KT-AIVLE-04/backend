package kt.aivle.content.repository;

import kt.aivle.content.dto.common.ContentDto;
import kt.aivle.content.dto.request.ContentSearchDto;
import kt.aivle.content.dto.response.ContentStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentRepositoryCustom {

    // 전체 콘텐츠 조회 (영상 + 이미지 통합)
    Page<ContentDto> findContentsWithFilter(ContentSearchDto searchDto, Pageable pageable);

    // 제목 기반 통합 검색 (영상 + 이미지)
    Page<ContentDto> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);

    // 콘텐츠 타입별 통계
    ContentStatsDto getContentStats();

    // 인기 콘텐츠 조회 (다운로드 수 기준 - 추후 다운로드 기능 구현 시)
    Page<ContentDto> findPopularContents(Pageable pageable);
}