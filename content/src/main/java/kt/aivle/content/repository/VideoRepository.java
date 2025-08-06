package kt.aivle.content.repository;

import kt.aivle.content.entity.Video;
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
public interface VideoRepository extends JpaRepository<Video, Long> {

    // === 기본 조회 메서드 ===

    /**
     * 삭제되지 않은 영상 조회 (ID로)
     */
    @Query("SELECT v FROM Video v WHERE v.id = :id AND v.deletedAt IS NULL")
    Optional<Video> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * 사용자별 영상 목록 조회 (삭제되지 않은 것만, 페이지네이션)
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    /**
     * 전체 영상 목록 조회 (삭제되지 않은 것만, 페이지네이션)
     */
    @Query("SELECT v FROM Video v WHERE v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findAllNotDeleted(Pageable pageable);

    // === 타입별 조회 ===

    /**
     * 영상 타입별 조회 (일반 영상/숏츠)
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.videoType = :videoType AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndVideoTypeAndNotDeleted(
            @Param("userId") Long userId,
            @Param("videoType") Video.VideoType videoType,
            Pageable pageable);

    /**
     * 숏츠 영상만 조회
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.isShorts = true AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findShortsByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    // === 처리 상태별 조회 ===

    /**
     * 처리 상태별 영상 조회
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.processingStatus = :status AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndProcessingStatusAndNotDeleted(
            @Param("userId") Long userId,
            @Param("status") Video.ProcessingStatus status,
            Pageable pageable);

    /**
     * 처리 완료된 영상만 조회
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.processingStatus = 'COMPLETED' AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findCompletedByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    // === 검색 기능 ===

    /**
     * 제목으로 영상 검색
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.title LIKE %:title% AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndTitleContainingAndNotDeleted(
            @Param("userId") Long userId,
            @Param("title") String title,
            Pageable pageable);

    /**
     * 시나리오 내용으로 영상 검색
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.scenario LIKE %:keyword% AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndScenarioContainingAndNotDeleted(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 제목 또는 시나리오로 통합 검색
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND (v.title LIKE %:keyword% OR v.scenario LIKE %:keyword%) AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> searchByKeywordAndNotDeleted(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // === 기간별 조회 ===

    /**
     * 생성 기간으로 영상 조회
     */
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.createdAt BETWEEN :startDate AND :endDate AND v.deletedAt IS NULL ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndCreatedAtBetweenAndNotDeleted(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // === 통계 및 카운트 ===

    /**
     * 사용자별 전체 영상 개수 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId AND v.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    /**
     * 사용자별 타입별 영상 개수
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId AND v.videoType = :videoType AND v.deletedAt IS NULL")
    long countByUserIdAndVideoTypeAndNotDeleted(
            @Param("userId") Long userId,
            @Param("videoType") Video.VideoType videoType);

    /**
     * 사용자별 처리 상태별 영상 개수
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.userId = :userId AND v.processingStatus = :status AND v.deletedAt IS NULL")
    long countByUserIdAndProcessingStatusAndNotDeleted(
            @Param("userId") Long userId,
            @Param("status") Video.ProcessingStatus status);

    /**
     * 사용자별 총 파일 크기
     */
    @Query("SELECT COALESCE(SUM(v.fileSize), 0) FROM Video v WHERE v.userId = :userId AND v.deletedAt IS NULL")
    long getTotalFileSizeByUserId(@Param("userId") Long userId);

    // === 파일 관리 ===

    /**
     * S3 파일 경로로 영상 조회 (중복 체크용)
     */
    @Query("SELECT v FROM Video v WHERE v.filePath = :filePath AND v.deletedAt IS NULL")
    Optional<Video> findByFilePathAndNotDeleted(@Param("filePath") String filePath);

    /**
     * 처리 중인 영상들 조회 (배치 작업용)
     */
    @Query("SELECT v FROM Video v WHERE v.processingStatus IN ('UPLOADING', 'PROCESSING') AND v.deletedAt IS NULL")
    List<Video> findProcessingVideos();

    /**
     * 오래된 처리 중 영상들 조회 (오류 처리용)
     */
    @Query("SELECT v FROM Video v WHERE v.processingStatus IN ('UPLOADING', 'PROCESSING') AND v.updatedAt < :cutoffTime AND v.deletedAt IS NULL")
    List<Video> findStuckProcessingVideos(@Param("cutoffTime") LocalDateTime cutoffTime);
}