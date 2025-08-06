//불 필요할 시 파일제거

package kt.aivle.content.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomMediaRepositoryImpl implements CustomMediaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public MediaStatistics getMediaStatisticsByUserId(Long userId) {
        String sql = """
                SELECT 
                    (SELECT COUNT(*) FROM videos WHERE user_id = ? AND deleted_at IS NULL) as total_videos,
                    (SELECT COUNT(*) FROM images WHERE user_id = ? AND deleted_at IS NULL) as total_images,
                    (SELECT COALESCE(SUM(file_size), 0) FROM videos WHERE user_id = ? AND deleted_at IS NULL) +
                    (SELECT COALESCE(SUM(file_size), 0) FROM images WHERE user_id = ? AND deleted_at IS NULL) as total_file_size,
                    (SELECT COUNT(*) FROM videos WHERE user_id = ? AND processing_status = 'COMPLETED' AND deleted_at IS NULL) as completed_videos,
                    (SELECT COUNT(*) FROM images WHERE user_id = ? AND processing_status = 'COMPLETED' AND deleted_at IS NULL) as completed_images,
                    (SELECT COUNT(*) FROM videos WHERE user_id = ? AND processing_status IN ('UPLOADING', 'PROCESSING') AND deleted_at IS NULL) as processing_videos,
                    (SELECT COUNT(*) FROM images WHERE user_id = ? AND processing_status IN ('UPLOADING', 'PROCESSING') AND deleted_at IS NULL) as processing_images
                """;

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 1; i <= 8; i++) {
            query.setParameter(i, userId);
        }

        Object[] result = (Object[]) query.getSingleResult();

        return new MediaStatistics(
                ((BigInteger) result[0]).longValue(),  // totalVideos
                ((BigInteger) result[1]).longValue(),  // totalImages
                ((BigInteger) result[2]).longValue(),  // totalFileSize
                ((BigInteger) result[3]).longValue(),  // completedVideos
                ((BigInteger) result[4]).longValue(),  // completedImages
                ((BigInteger) result[5]).longValue(),  // processingVideos
                ((BigInteger) result[6]).longValue()   // processingImages
        );
    }

    @Override
    public List<Object> getRecentMediaByUserId(Long userId, int limit) {
        String sql = """
                (SELECT 'video' as type, id, title, file_url, thumbnail_url, created_at 
                 FROM videos 
                 WHERE user_id = ? AND deleted_at IS NULL AND processing_status = 'COMPLETED')
                UNION ALL
                (SELECT 'image' as type, id, title, file_url, thumbnail_url, created_at 
                 FROM images 
                 WHERE user_id = ? AND deleted_at IS NULL AND processing_status = 'COMPLETED')
                ORDER BY created_at DESC
                LIMIT ?
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, userId);
        query.setParameter(2, userId);
        query.setParameter(3, limit);

        return query.getResultList();
    }

    @Override
    public List<DailyUploadStats> getDailyUploadStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
                SELECT 
                    DATE(created_at) as upload_date,
                    SUM(CASE WHEN table_type = 'video' THEN 1 ELSE 0 END) as video_count,
                    SUM(CASE WHEN table_type = 'image' THEN 1 ELSE 0 END) as image_count,
                    SUM(file_size) as total_size
                FROM (
                    SELECT created_at, file_size, 'video' as table_type 
                    FROM videos 
                    WHERE user_id = ? AND deleted_at IS NULL 
                    AND created_at BETWEEN ? AND ?
                
                    UNION ALL
                
                    SELECT created_at, file_size, 'image' as table_type 
                    FROM images 
                    WHERE user_id = ? AND deleted_at IS NULL 
                    AND created_at BETWEEN ? AND ?
                ) combined
                GROUP BY DATE(created_at)
                ORDER BY upload_date DESC
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, userId);
        query.setParameter(2, startDate);
        query.setParameter(3, endDate);
        query.setParameter(4, userId);
        query.setParameter(5, startDate);
        query.setParameter(6, endDate);

        List<Object[]> results = query.getResultList();
        List<DailyUploadStats> stats = new ArrayList<>();

        for (Object[] row : results) {
            LocalDateTime date = ((java.sql.Date) row[0]).toLocalDate().atStartOfDay();
            Long videoCount = ((BigInteger) row[1]).longValue();
            Long imageCount = ((BigInteger) row[2]).longValue();
            Long totalSize = row[3] != null ? ((BigInteger) row[3]).longValue() : 0L;

            stats.add(new DailyUploadStats(date, videoCount, imageCount, totalSize));
        }

        return stats;
    }

    @Override
    public List<FileSizeDistribution> getFileSizeDistribution(Long userId) {
        String sql = """
                SELECT 
                    size_range,
                    SUM(CASE WHEN table_type = 'video' THEN count_val ELSE 0 END) as video_count,
                    SUM(CASE WHEN table_type = 'image' THEN count_val ELSE 0 END) as image_count
                FROM (
                    SELECT 
                        CASE 
                            WHEN file_size < 1048576 THEN '< 1MB'
                            WHEN file_size < 10485760 THEN '1MB - 10MB'
                            WHEN file_size < 52428800 THEN '10MB - 50MB'
                            WHEN file_size < 104857600 THEN '50MB - 100MB'
                            WHEN file_size < 524288000 THEN '100MB - 500MB'
                            ELSE '> 500MB'
                        END as size_range,
                        COUNT(*) as count_val,
                        'video' as table_type
                    FROM videos 
                    WHERE user_id = ? AND deleted_at IS NULL
                    GROUP BY size_range
                
                    UNION ALL
                
                    SELECT 
                        CASE 
                            WHEN file_size < 1048576 THEN '< 1MB'
                            WHEN file_size < 10485760 THEN '1MB - 10MB'
                            WHEN file_size < 52428800 THEN '10MB - 50MB'
                            WHEN file_size < 104857600 THEN '50MB - 100MB'
                            WHEN file_size < 524288000 THEN '100MB - 500MB'
                            ELSE '> 500MB'
                        END as size_range,
                        COUNT(*) as count_val,
                        'image' as table_type
                    FROM images 
                    WHERE user_id = ? AND deleted_at IS NULL
                    GROUP BY size_range
                ) combined
                GROUP BY size_range
                ORDER BY 
                    CASE size_range
                        WHEN '< 1MB' THEN 1
                        WHEN '1MB - 10MB' THEN 2
                        WHEN '10MB - 50MB' THEN 3
                        WHEN '50MB - 100MB' THEN 4
                        WHEN '100MB - 500MB' THEN 5
                        WHEN '> 500MB' THEN 6
                    END
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, userId);
        query.setParameter(2, userId);

        List<Object[]> results = query.getResultList();
        List<FileSizeDistribution> distributions = new ArrayList<>();

        for (Object[] row : results) {
            String sizeRange = (String) row[0];
            Long videoCount = ((BigInteger) row[1]).longValue();
            Long imageCount = ((BigInteger) row[2]).longValue();

            distributions.add(new FileSizeDistribution(sizeRange, videoCount, imageCount));
        }

        return distributions;
    }

    @Override
    public StorageUsageDetail getStorageUsageDetail(Long userId) {
        String sql = """
                SELECT 
                    (SELECT COALESCE(SUM(file_size), 0) FROM videos WHERE user_id = ? AND deleted_at IS NULL) as video_size,
                    (SELECT COALESCE(SUM(file_size), 0) FROM images WHERE user_id = ? AND deleted_at IS NULL) as image_size,
                    -- 썸네일 크기는 추정치 (원본의 5% 가정)
                    ((SELECT COALESCE(SUM(file_size), 0) FROM videos WHERE user_id = ? AND deleted_at IS NULL) +
                     (SELECT COALESCE(SUM(file_size), 0) FROM images WHERE user_id = ? AND deleted_at IS NULL)) * 0.05 as thumbnail_size
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, userId);
        query.setParameter(2, userId);
        query.setParameter(3, userId);
        query.setParameter(4, userId);

        Object[] result = (Object[]) query.getSingleResult();

        Long videoSize = ((BigInteger) result[0]).longValue();
        Long imageSize = ((BigInteger) result[1]).longValue();
        Long thumbnailSize = ((Double) result[2]).longValue();
        Long totalSize = videoSize + imageSize + thumbnailSize;

        return new StorageUsageDetail(totalSize, videoSize, imageSize, thumbnailSize);
    }
}