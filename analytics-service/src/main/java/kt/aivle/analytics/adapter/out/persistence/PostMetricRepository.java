package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.PostMetric;
import kt.aivle.analytics.domain.port.out.PostMetricRepositoryPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostMetricRepository implements PostMetricRepositoryPort {
    
    private final PostMetricJpaRepository jpaRepository;
    
    @Override
    public List<PostMetric> findByUserIdAndSocialPostIdAndDateRange(String userId, Long socialPostId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUserIdAndSocialPostIdAndMetricDateBetweenOrderByMetricDateDesc(userId, socialPostId, startDate, endDate);
    }
    
    @Override
    public List<PostMetric> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUserIdAndMetricDateBetweenOrderByMetricDateDesc(userId, startDate, endDate);
    }
    
    @Override
    public List<PostMetric> findTopPerformingByUserId(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "engagementRate"));
        return jpaRepository.findByUserIdOrderByEngagementRateDesc(userId, pageable);
    }
    
    @Override
    public Optional<PostMetric> findBySocialPostIdAndMetricDate(Long socialPostId, LocalDate metricDate) {
        return jpaRepository.findBySocialPostIdAndMetricDate(socialPostId, metricDate);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
    
    @Override
    public PostMetric save(PostMetric postMetric) {
        return jpaRepository.save(postMetric);
    }
    
    @Override
    public List<PostMetric> saveAll(List<PostMetric> postMetrics) {
        return jpaRepository.saveAll(postMetrics);
    }
}
