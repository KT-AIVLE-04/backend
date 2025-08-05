package kt.aivle.content.repository;

import kt.aivle.content.domain.ImageContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageContentRepository extends JpaRepository<ImageContent, Long> {

    // 삭제되지 않은 이미지 목록 조회 (페이지네이션)
    Page<ImageContent> findByIsDeletedFalseOrderByCreatedDateDesc(Pageable pageable);

    // 삭제되지 않은 이미지 상세 조회
    Optional<ImageContent> findByIdAndIsDeletedFalse(Long id);

    // 제목으로 검색 (삭제되지 않은 것만)
    Page<ImageContent> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);

    // 키워드로 검색
    Page<ImageContent> findByKeywordsContainingIgnoreCaseAndIsDeletedFalse(String keywords, Pageable pageable);

    // AI 생성 여부로 필터링
    Page<ImageContent> findByIsAiGeneratedAndIsDeletedFalse(Boolean isAiGenerated, Pageable pageable);

    // 기간별 조회
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false AND i.createdDate BETWEEN :startDate AND :endDate ORDER BY i.createdDate DESC")
    Page<ImageContent> findByCreatedDateBetweenAndIsDeletedFalse(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 이미지 포맷별 조회
    Page<ImageContent> findByImageFormatAndIsDeletedFalse(String imageFormat, Pageable pageable);

    // 특정 해상도 이상의 이미지 조회
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false AND i.width >= :minWidth AND i.height >= :minHeight ORDER BY i.createdDate DESC")
    Page<ImageContent> findByMinResolutionAndIsDeletedFalse(
            @Param("minWidth") Integer minWidth,
            @Param("minHeight") Integer minHeight,
            Pageable pageable);

    // 압축된 이미지 조회
    Page<ImageContent> findByIsCompressedAndIsDeletedFalse(Boolean isCompressed, Pageable pageable);

    // 최근 업로드된 이미지 조회 (개수 제한)
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false ORDER BY i.createdDate DESC")
    List<ImageContent> findRecentImages(Pageable pageable);

    // 이미지 통계 (AI 생성 여부별)
    @Query("SELECT i.isAiGenerated, COUNT(i) FROM ImageContent i WHERE i.isDeleted = false GROUP BY i.isAiGenerated")
    List<Object[]> getImageStatsByAiGenerated();

    // 월별 이미지 업로드 통계
    @Query("SELECT YEAR(i.createdDate), MONTH(i.createdDate), COUNT(i) FROM ImageContent i WHERE i.isDeleted = false GROUP BY YEAR(i.createdDate), MONTH(i.createdDate) ORDER BY YEAR(i.createdDate) DESC, MONTH(i.createdDate) DESC")
    List<Object[]> getMonthlyImageStats();

    // 복합 검색 쿼리
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false " +
            "AND (:title IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:keywords IS NULL OR LOWER(i.keywords) LIKE LOWER(CONCAT('%', :keywords, '%'))) " +
            "AND (:isAiGenerated IS NULL OR i.isAiGenerated = :isAiGenerated) " +
            "AND (:imageFormat IS NULL OR i.imageFormat = :imageFormat) " +
            "AND (:startDate IS NULL OR i.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR i.createdDate <= :endDate) " +
            "ORDER BY i.createdDate DESC")
    Page<ImageContent> findImagesWithFilters(
            @Param("title") String title,
            @Param("keywords") String keywords,
            @Param("isAiGenerated") Boolean isAiGenerated,
            @Param("imageFormat") String imageFormat,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 파일 크기 범위로 조회
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false AND i.fileSize BETWEEN :minSize AND :maxSize ORDER BY i.fileSize DESC")
    Page<ImageContent> findByFileSizeBetweenAndIsDeletedFalse(
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize,
            Pageable pageable);

    // DPI별 조회
    Page<ImageContent> findByDpiAndIsDeletedFalse(Integer dpi, Pageable pageable);

    // 색상 공간별 조회
    Page<ImageContent> findByColorSpaceAndIsDeletedFalse(String colorSpace, Pageable pageable);

    // 중복 파일명 체크
    boolean existsByFileNameAndIsDeletedFalse(String fileName);

    // 소프트 삭제된 이미지들 조회 (관리자용)
    Page<ImageContent> findByIsDeletedTrueOrderByDeletedDateDesc(Pageable pageable);

    // 특정 기간 이전에 삭제된 이미지들 조회 (정리용)
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = true AND i.deletedDate < :beforeDate")
    List<ImageContent> findDeletedImagesBefore(@Param("beforeDate") LocalDateTime beforeDate);

    // 태그(키워드) 기반 관련 이미지 찾기
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false AND i.id != :excludeId " +
            "AND (:keywords IS NULL OR LOWER(i.keywords) LIKE LOWER(CONCAT('%', :keywords, '%'))) " +
            "ORDER BY i.createdDate DESC")
    List<ImageContent> findRelatedImages(@Param("excludeId") Long excludeId, @Param("keywords") String keywords, Pageable pageable);

    // 해상도 범위로 조회
    @Query("SELECT i FROM ImageContent i WHERE i.isDeleted = false " +
            "AND i.width BETWEEN :minWidth AND :maxWidth " +
            "AND i.height BETWEEN :minHeight AND :maxHeight " +
            "ORDER BY i.createdDate DESC")
    Page<ImageContent> findByResolutionRange(
            @Param("minWidth") Integer minWidth,
            @Param("maxWidth") Integer maxWidth,
            @Param("minHeight") Integer minHeight,
            @Param("maxHeight") Integer maxHeight,
            Pageable pageable);
}