package kt.aivle.content.repository;

import kt.aivle.content.entity.Content;
import kt.aivle.content.entity.ContentType;
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
public interface ContentRepository extends JpaRepository<Content, Long> {

    // 사용자별 콘텐츠 조회 (페이징)
    Page<Content> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 사용자별 특정 타입 콘텐츠 조회 (페이징)
    Page<Content> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, ContentType type, Pageable pageable);

    // 사용자별 콘텐츠 개수
    long countByUserId(String userId);

    // 사용자별 특정 타입 콘텐츠 개수
    long countByUserIdAndType(String userId, ContentType type);

    // S3 키로 콘텐츠 찾기 (삭제 시 사용)
    Optional<Content> findByS3Key(String s3Key);

    // 사용자의 특정 기간 콘텐츠 조회
    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Content> findByUserIdAndDateRange(@Param("userId") String userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // 제목으로 검색 (사용자별)
    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.title LIKE %:title% ORDER BY c.createdAt DESC")
    Page<Content> findByUserIdAndTitleContaining(@Param("userId") String userId,
                                                 @Param("title") String title,
                                                 Pageable pageable);

    // 파일 크기별 조회
    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.fileSize BETWEEN :minSize AND :maxSize ORDER BY c.createdAt DESC")
    Page<Content> findByUserIdAndFileSizeRange(@Param("userId") String userId,
                                               @Param("minSize") Long minSize,
                                               @Param("maxSize") Long maxSize,
                                               Pageable pageable);

    // 최근 업로드된 콘텐츠 (전체 사용자, 관리자용)
    @Query("SELECT c FROM Content c ORDER BY c.createdAt DESC")
    Page<Content> findAllOrderByCreatedAtDesc(Pageable pageable);

    // 사용자별 총 파일 크기
    @Query("SELECT COALESCE(SUM(c.fileSize), 0) FROM Content c WHERE c.userId = :userId")
    Long getTotalFileSizeByUserId(@Param("userId") String userId);

    // 월별 업로드 통계
    @Query("SELECT DATE_FORMAT(c.createdAt, '%Y-%m') as month, COUNT(c) as count " +
            "FROM Content c WHERE c.userId = :userId " +
            "GROUP BY DATE_FORMAT(c.createdAt, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Object[]> getMonthlyUploadStats(@Param("userId") String userId);
}