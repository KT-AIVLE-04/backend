package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.PostMetricJpaRepository;
import kt.aivle.analytics.application.port.out.PostMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.PostMetric;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostMetricRepository implements PostMetricRepositoryPort {
    
    private final PostMetricJpaRepository postMetricJpaRepository;
    
    @Override
    public PostMetric save(PostMetric postMetric) {
        return postMetricJpaRepository.save(postMetric);
    }
    
    @Override
    public Optional<PostMetric> findById(Long id) {
        return postMetricJpaRepository.findById(id);
    }
    
    @Override
    public List<PostMetric> findByPostId(Long postId) {
        return postMetricJpaRepository.findByPostId(postId);
    }
    
    @Override
    public List<PostMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate) {
        return postMetricJpaRepository.findByPostIdAndCrawledAtBetween(postId, startDate, endDate);
    }
    
    @Override
    public List<PostMetric> findByAccountIdAndCrawledAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return postMetricJpaRepository.findByAccountIdAndCrawledAtBetween(accountId, startDate, endDate);
    }
    
    @Override
    public Optional<PostMetric> findLatestByPostId(Long postId) {
        return postMetricJpaRepository.findLatestByPostId(postId);
    }
    
    @Override
    public void deleteByPostId(Long postId) {
        postMetricJpaRepository.deleteByPostId(postId);
    }
}
