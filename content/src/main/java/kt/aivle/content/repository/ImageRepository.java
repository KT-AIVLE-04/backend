package kt.aivle.content.repository;

import kt.aivle.content.entity.Image;
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
public interface ImageRepository extends JpaRepository<Image, Long> {

    // === 기본 조회 메서드 ===

    /**
     * 삭제되지 않은 이미지 조회 (ID로)
     */
    @Query("SELECT i FROM Image i WHERE i.id = :id AND i.deletedAt IS NULL")
    Optional<Image> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * 사용자별 이미지 목록 조회 (삭제되지 않은 것만, 페이지네이션)
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    /**
     * 전체 이미지 목록 조회 (삭제되지 않은 것만, 페이지네이션)
     */
    @Query("SELECT i FROM Image i WHERE i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findAllNotDeleted(Pageable pageable);

    // === 포맷별 조회 ===

    /**
     * 이미지 포맷별 조회
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.imageFormat = :format AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndImageFormatAndNotDeleted(
            @Param("userId") Long userId,
            @Param("format") Image.ImageFormat format,
            Pageable pageable);

    // === 처리 상태별 조회 ===

    /**
     * 처리 상태별 이미지 조회
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.processingStatus = :status AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndProcessingStatusAndNotDeleted(
            @Param("userId") Long userId,
            @Param("status") Image.ProcessingStatus status,
            Pageable pageable);

    /**
     * 처리 완료된 이미지만 조회
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.processingStatus = 'COMPLETED' AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findCompletedByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    // === 검색 기능 ===

    /**
     * 제목으로 이미지 검색
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.title LIKE %:title% AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndTitleContainingAndNotDeleted(
            @Param("userId") Long userId,
            @Param("title") String title,
            Pageable pageable);

    /**
     * 키워드로 이미지 검색 (FULLTEXT 검색)
     */
    @Query(value = "SELECT * FROM images WHERE user_id = :userId AND MATCH(keywords) AGAINST(:keyword IN NATURAL LANGUAGE MODE) AND deleted_at IS NULL ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM images WHERE user_id = :userId AND MATCH(keywords) AGAINST(:keyword IN NATURAL LANGUAGE MODE) AND deleted_at IS NULL",
            nativeQuery = true)
    Page<Image> searchByKeywordsFullText(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 키워드로 이미지 검색 (LIKE 검색 - FULLTEXT 미지원시 대체)
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.keywords LIKE %:keyword% AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndKeywordsContainingAndNotDeleted(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 제목 또는 키워드로 통합 검색
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND (i.title LIKE %:keyword% OR i.keywords LIKE %:keyword%) AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> searchByKeywordAndNotDeleted(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // === 크기 및 해상도별 조회 ===

    /**
     * 특정 해상도 이상의 이미지 조회
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.width >= :minWidth AND i.height >= :minHeight AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndMinResolutionAndNotDeleted(
            @Param("userId") Long userId,
            @Param("minWidth") Integer minWidth,
            @Param("minHeight") Integer minHeight,
            Pageable pageable);

    /**
     * 파일 크기 범위로 이미지 조회
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.fileSize BETWEEN :minSize AND :maxSize AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndFileSizeBetweenAndNotDeleted(
            @Param("userId") Long userId,
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize,
            Pageable pageable);

    // === 기간별 조회 ===

    /**
     * 생성 기간으로 이미지 조회
     */
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.createdAt BETWEEN :startDate AND :endDate AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndCreatedAtBetweenAndNotDeleted(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // === 통계 및 카운트 ===

    /**
     * 사용자별 전체 이미지 개수 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(i) FROM Image i WHERE i.userId = :userId AND i.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    /**
     * 사용자별 포맷별 이미지 개수
     */
    @Query("SELECT COUNT(i) FROM Image i WHERE i.userId = :userId AND i.imageFormat = :format AND i.deletedAt IS NULL")
    long countByUserIdAndImageFormatAndNotDeleted(
            @Param("userId") Long userId,
            @Param("format") Image.ImageFormat format);

    /**
     * 사용자별 처리 상태별 이미지 개수
     */
    @Query("SELECT COUNT(i) FROM Image i WHERE i.userId = :userId AND i.processingStatus = :status AND i.deletedAt IS NULL")
    long countByUserIdAndProcessingStatusAndNotDeleted(
            @Param("userId") Long userId,
            @Param("status") Image.ProcessingStatus status);

    /**
     * 사용자별 총 파일 크기
     */
    @Query("SELECT COALESCE(SUM(i.fileSize), 0) FROM Image i WHERE i.userId = :userId AND i.deletedAt IS NULL")
    long getTotalFileSizeByUserId(@Param("userId") Long userId);

    /**
     * 사용자별 평균 이미지 크기
     */
    @Query("SELECT AVG(i.fileSize) FROM Image i WHERE i.userId = :userId AND i.deletedAt IS NULL")
    Double getAverageFileSizeByUserId(@Param("userId") Long userId);

    // === 파일 관리 ===

    /**
     * S3 파일 경로로 이미지 조회 (중복 체크용)
     */
    @Query("SELECT i FROM Image i WHERE i.filePath = :filePath AND i.deletedAt IS NULL")
    Optional<Image> findByFilePathAndNotDeleted(@Param("filePath") String filePath);

    /**
     * 처리 중인 이미지들 조회 (배치 작업용)
     */
    @Query("SELECT i FROM Image i WHERE i.processingStatus IN ('UPLOADING', 'PROCESSING') AND i.deletedAt IS NULL")
    List<Image> findProcessingImages();

    /**
     * 오래된 처리 중 이미지들 조회 (오류 처리용)
     */
    @Query("SELECT i FROM Image i WHERE i.processingStatus IN ('UPLOADING', 'PROCESSING') AND i.updatedAt < :cutoffTime AND i.deletedAt IS NULL")
    List<Image> findStuckProcessingImages(@Param("cutoffTime") LocalDateTime cutoffTime);

    // === 고급 검색 ===

    /**
     * 여러 키워드로 이미지 검색 (AND 조건)
     */
    @Query(value = "SELECT * FROM images WHERE user_id = :userId AND " +
            "(:keyword1 IS NULL OR keywords LIKE CONCAT('%', :keyword1, '%')) AND " +
            "(:keyword2 IS NULL OR keywords LIKE CONCAT('%', :keyword2, '%')) AND " +
            "(:keyword3 IS NULL OR keywords LIKE CONCAT('%', :keyword3, '%')) AND " +
            "deleted_at IS NULL ORDER BY created_at DESC",
            nativeQuery = true)
    Page<Image> searchByMultipleKeywords(
            @Param("userId") Long userId,
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2,
            @Param("keyword3") String keyword3,
            Pageable pageable);
}