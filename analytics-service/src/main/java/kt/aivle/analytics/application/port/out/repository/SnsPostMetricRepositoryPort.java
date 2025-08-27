package kt.aivle.analytics.application.port.out.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsPostMetric;

public interface SnsPostMetricRepositoryPort {
    SnsPostMetric save(SnsPostMetric snsPostMetric);
    Optional<SnsPostMetric> findById(Long id);
    List<SnsPostMetric> findByPostIdAndCreatedAtDate(Long postId, LocalDate date);
    
    /**
     * Post Metrics를 Post와 Account 정보와 함께 조회 (JOIN 쿼리)
     */
    List<Object[]> findMetricsWithPostAndAccount(List<Long> postIds, LocalDate date);
    
    boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDateTime date);
    void deleteById(Long id);
    
    /**
     * 특정 게시물의 가장 최근 메트릭 조회
     */
    Optional<SnsPostMetric> findLatestByPostId(Long postId);
}
