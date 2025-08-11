package kt.aivle.content.repository;

import kt.aivle.content.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // 사용자별 이미지 조회 (페이징, 최신순)
    Page<Image> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // 사용자별 이미지 개수
    long countByUserId(String userId);

    // 특정 해상도 범위의 이미지 조회
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.width >= :minWidth AND i.height >= :minHeight ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndMinResolution(@Param("userId") String userId,
                                             @Param("minWidth") Integer minWidth,
                                             @Param("minHeight") Integer minHeight,
                                             Pageable pageable);

    // 가로/세로 비율별 이미지 조회
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND " +
            "CASE WHEN :aspectRatio = 'landscape' THEN i.width > i.height " +
            "     WHEN :aspectRatio = 'portrait' THEN i.width < i.height " +
            "     WHEN :aspectRatio = 'square' THEN i.width = i.height " +
            "     ELSE TRUE END " +
            "ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndAspectRatio(@Param("userId") String userId,
                                           @Param("aspectRatio") String aspectRatio,
                                           Pageable pageable);

    // 큰 이미지 조회 (파일 크기 기준)
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.fileSize > :sizeThreshold ORDER BY i.fileSize DESC")
    List<Image> findLargeImagesByUserId(@Param("userId") String userId, @Param("sizeThreshold") Long sizeThreshold);

    // 썸네일이 없는 이미지 조회 (썸네일 생성 배치용)
    @Query("SELECT i FROM Image i WHERE i.thumbnailUrl IS NULL OR i.thumbnailUrl = ''")
    List<Image> findImagesWithoutThumbnail();

    // 이미지 해상도별 통계
    @Query("SELECT " +
            "CASE WHEN i.width >= 3840 THEN '4K+' " +
            "     WHEN i.width >= 1920 THEN 'FHD' " +
            "     WHEN i.width >= 1280 THEN 'HD' " +
            "     ELSE 'SD' END as resolution, " +
            "COUNT(i) as count " +
            "FROM Image i WHERE i.userId = :userId " +
            "GROUP BY CASE WHEN i.width >= 3840 THEN '4K+' " +
            "              WHEN i.width >= 1920 THEN 'FHD' " +
            "              WHEN i.width >= 1280 THEN 'HD' " +
            "              ELSE 'SD' END")
    List<Object[]> getResolutionStats(@Param("userId") String userId);

    // 최근 업로드된 이미지 (미리보기용)
    @Query("SELECT i FROM Image i WHERE i.userId = :userId ORDER BY i.createdAt DESC")
    List<Image> findRecentImagesByUserId(@Param("userId") String userId, Pageable pageable);

    // S3 키로 이미지 찾기
    Optional<Image> findByS3Key(String s3Key);

    // 썸네일 S3 키로 이미지 찾기
    Optional<Image> findByThumbnailS3Key(String thumbnailS3Key);

    // 원본 파일명으로 검색
    @Query("SELECT i FROM Image i WHERE i.userId = :userId AND i.originalFilename LIKE %:filename% ORDER BY i.createdAt DESC")
    Page<Image> findByUserIdAndOriginalFilenameContaining(@Param("userId") String userId,
                                                          @Param("filename") String filename,
                                                          Pageable pageable);
}