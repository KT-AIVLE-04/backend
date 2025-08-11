package kt.aivle.content.repository;

import kt.aivle.content.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    // 사용자별 영상 조회 (페이징, 최신순)
    Page<Video> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 사용자별 영상 개수
    long countByUserId(String userId);

    // 숏츠 영상만 조회
    Page<Video> findByUserIdAndIsShortTrueOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 일반 영상만 조회 (숏츠 제외)
    Page<Video> findByUserIdAndIsShortFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 숏츠 개수
    long countByUserIdAndIsShortTrue(String userId);

    // 영상 길이별 조회
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.durationSeconds BETWEEN :minDuration AND :maxDuration ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndDurationRange(@Param("userId") String userId,
                                             @Param("minDuration") Integer minDuration,
                                             @Param("maxDuration") Integer maxDuration,
                                             Pageable pageable);

    // 특정 해상도 이상의 영상 조회
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.width >= :minWidth AND v.height >= :minHeight ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndMinResolution(@Param("userId") String userId,
                                             @Param("minWidth") Integer minWidth,
                                             @Param("minHeight") Integer minHeight,
                                             Pageable pageable);

    // 코덱별 영상 조회
    Page<Video> findByUserIdAndCodecOrderByCreatedAtDesc(String userId, String codec, Pageable pageable);

    // 긴 영상 조회 (파일 크기 기준)
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.fileSize > :sizeThreshold ORDER BY v.fileSize DESC")
    List<Video> findLargeVideosByUserId(@Param("userId") String userId, @Param("sizeThreshold") Long sizeThreshold);

    // 썸네일이 없는 영상 조회 (썸네일 생성 배치용)
    @Query("SELECT v FROM Video v WHERE v.thumbnailUrl IS NULL OR v.thumbnailUrl = ''")
    List<Video> findVideosWithoutThumbnail();

    // 영상 해상도별 통계
    @Query("SELECT " +
            "CASE WHEN v.width >= 3840 THEN '4K' " +
            "     WHEN v.width >= 1920 THEN '1080p' " +
            "     WHEN v.width >= 1280 THEN '720p' " +
            "     WHEN v.width >= 854 THEN '480p' " +
            "     ELSE '360p' END as resolution, " +
            "COUNT(v) as count " +
            "FROM Video v WHERE v.userId = :userId " +
            "GROUP BY CASE WHEN v.width >= 3840 THEN '4K' " +
            "              WHEN v.width >= 1920 THEN '1080p' " +
            "              WHEN v.width >= 1280 THEN '720p' " +
            "              WHEN v.width >= 854 THEN '480p' " +
            "              ELSE '360p' END")
    List<Object[]> getResolutionStats(@Param("userId") String userId);

    // 영상 길이별 통계
    @Query("SELECT " +
            "CASE WHEN v.durationSeconds <= 60 THEN 'Shorts' " +
            "     WHEN v.durationSeconds <= 300 THEN 'Short' " +
            "     WHEN v.durationSeconds <= 1800 THEN 'Medium' " +
            "     ELSE 'Long' END as duration_category, " +
            "COUNT(v) as count " +
            "FROM Video v WHERE v.userId = :userId " +
            "GROUP BY CASE WHEN v.durationSeconds <= 60 THEN 'Shorts' " +
            "              WHEN v.durationSeconds <= 300 THEN 'Short' " +
            "              WHEN v.durationSeconds <= 1800 THEN 'Medium' " +
            "              ELSE 'Long' END")
    List<Object[]> getDurationStats(@Param("userId") String userId);

    // 총 영상 재생 시간 (사용자별)
    @Query("SELECT COALESCE(SUM(v.durationSeconds), 0) FROM Video v WHERE v.userId = :userId")
    Long getTotalDurationByUserId(@Param("userId") String userId);

    // 최근 업로드된 영상 (미리보기용)
    @Query("SELECT v FROM Video v WHERE v.userId = :userId ORDER BY v.createdAt DESC")
    List<Video> findRecentVideosByUserId(@Param("userId") String userId, Pageable pageable);

    // S3 키로 영상 찾기
    Optional<Video> findByS3Key(String s3Key);

    // 썸네일 S3 키로 영상 찾기
    Optional<Video> findByThumbnailS3Key(String thumbnailS3Key);

    // 원본 파일명으로 검색
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.originalFilename LIKE %:filename% ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndOriginalFilenameContaining(@Param("userId") String userId,
                                                          @Param("filename") String filename,
                                                          Pageable pageable);

    // 특정 비트레이트 범위의 영상 조회
    @Query("SELECT v FROM Video v WHERE v.userId = :userId AND v.bitrate BETWEEN :minBitrate AND :maxBitrate ORDER BY v.createdAt DESC")
    Page<Video> findByUserIdAndBitrateRange(@Param("userId") String userId,
                                            @Param("minBitrate") Integer minBitrate,
                                            @Param("maxBitrate") Integer maxBitrate,
                                            Pageable pageable);
}