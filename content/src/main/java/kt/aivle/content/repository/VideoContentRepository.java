package kt.aivle.content.repository;

import kt.aivle.content.domain.VideoContent;
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
public interface VideoContentRepository extends JpaRepository<VideoContent, Long> {

    // 삭제되지 않은 영상 목록 조회 (페이지네이션)
    Page<VideoContent> findByIsDeletedFalseOrderByCreatedDateDesc(Pageable pageable);

    // 삭제되지 않은 영상 상세 조회
    Optional<VideoContent> findByIdAndIsDeletedFalse(Long id);

    // 제목으로 검색 (삭제되지 않은 것만)
    Page<VideoContent> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);

    // AI 생성 여부로 필터링
    Page<VideoContent> findByIsAiGeneratedAndIsDeletedFalse(Boolean isAiGenerated, Pageable pageable);

    // 숏츠만 조회
    Page<VideoContent> findByIsShortsAndIsDeletedFalse(Boolean isShorts, Pageable pageable);

    // 기간별 조회
    @Query("SELECT v FROM VideoContent v WHERE v.isDeleted = false AND v.createdDate BETWEEN :startDate AND :endDate ORDER BY v.createdDate DESC")
    Page<VideoContent> findByCreatedDateBetweenAndIsDeletedFalse(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 영상 포맷별 조회
    Page<VideoContent> findByVideoFormatAndIsDeletedFalse(String videoFormat, Pageable pageable);

    // 특정 해상도 이상의 영상 조회
    @Query("SELECT v FROM VideoContent v WHERE v.isDeleted = false AND v.width >= :minWidth AND v.height >= :minHeight ORDER BY v.createdDate DESC")
    Page<VideoContent> findByMinResolutionAndIsDeletedFalse(
            @Param("minWidth") Integer minWidth,
            @Param("minHeight") Integer minHeight,
            Pageable pageable);

    // 최근 업로드된 영상 조회 (개수 제한)
    @Query("SELECT v FROM VideoContent v WHERE v.isDeleted = false ORDER BY v.createdDate DESC")
    List<VideoContent> findRecentVideos(Pageable pageable);

    // 사용자별 영상 개수 통계 (AI 생성 여부별)
    @Query("SELECT v.isAiGenerated, COUNT(v) FROM VideoContent v WHERE v.isDeleted = false GROUP BY v.isAiGenerated")
    List<Object[]> getVideoStatsByAiGenerated();

    // 월별 영상 업로드 통계
    @Query("SELECT YEAR(v.createdDate), MONTH(v.createdDate), COUNT(v) FROM VideoContent v WHERE v.isDeleted = false GROUP BY YEAR(v.createdDate), MONTH(v.createdDate) ORDER BY YEAR(v.createdDate) DESC, MONTH(v.createdDate) DESC")
    List<Object[]> getMonthlyVideoStats();

    // 복합 검색 쿼리
    @Query("SELECT v FROM VideoContent v WHERE v.isDeleted = false " +
            "AND (:title IS NULL OR LOWER(v.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:isAiGenerated IS NULL OR v.isAiGenerated = :isAiGenerated) " +
            "AND (:isShorts IS NULL OR v.isShorts = :isShorts) " +
            "AND (:videoFormat IS NULL OR v.videoFormat = :videoFormat) " +
            "AND (:startDate IS NULL OR v.createdDate >= :startDate) " +
            "AND (:endDate IS NULL OR v.createdDate <= :endDate) " +
            "ORDER BY v.createdDate DESC")
    Page<VideoContent> findVideosWithFilters(
            @Param("title") String title,
            @Param("isAiGenerated") Boolean isAiGenerated,
            @Param("isShorts") Boolean isShorts,
            @Param("videoFormat") String videoFormat,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 썸네일이 없는 영상 조회 (썸네일 생성이 필요한 영상들)
    List<VideoContent> findByThumbnailPathIsNullAndIsDeletedFalse();

    // 파일 크기 범위로 조회
    @Query("SELECT v FROM VideoContent v WHERE v.isDeleted = false AND v.fileSize BETWEEN :minSize AND :maxSize ORDER BY v.fileSize DESC")
    Page<VideoContent> findByFileSizeBetweenAndIsDeletedFalse(
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize,
            Pageable pageable);

    // 중복 파일명 체크
    boolean existsByFileNameAndIsDeletedFalse(String fileName);

    // 소프트 삭제된 영상들 조회 (관리자용)
    Page<VideoContent> findByIsDeletedTrueOrderByDeletedDateDesc(Pageable pageable);

    // 특정 기간 이전에 삭제된 영상들 조회 (정리용)
    @Query("SELECT v FROM VideoContent v WHERE v.isDeleted = true AND v.deletedDate < :beforeDate")
    List<VideoContent> findDeletedVideosBefore(@Param("beforeDate") LocalDateTime beforeDate);
}