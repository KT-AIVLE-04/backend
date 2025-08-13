package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.PostMetric;

@Repository
public interface PostMetricJpaRepository extends JpaRepository<PostMetric, Long> {
    
    List<PostMetric> findByUserIdAndSocialPostIdAndMetricDateBetweenOrderByMetricDateDesc(
        String userId, Long socialPostId, LocalDate startDate, LocalDate endDate);
    
    List<PostMetric> findByUserIdAndMetricDateBetweenOrderByMetricDateDesc(
        String userId, LocalDate startDate, LocalDate endDate);
    
    // 동적 limit/page 처리
    List<PostMetric> findByUserIdOrderByEngagementRateDesc(String userId, Pageable pageable);
    
    Optional<PostMetric> findBySocialPostIdAndMetricDate(Long socialPostId, LocalDate metricDate);
    
    void deleteByUserId(String userId);
}
