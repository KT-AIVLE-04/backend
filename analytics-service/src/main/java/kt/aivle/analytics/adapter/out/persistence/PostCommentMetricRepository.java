package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.PostCommentMetricJpaRepository;
import kt.aivle.analytics.application.port.out.PostCommentMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.PostCommentMetric;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostCommentMetricRepository implements PostCommentMetricRepositoryPort {
    
    private final PostCommentMetricJpaRepository postCommentMetricJpaRepository;
    
    @Override
    public PostCommentMetric save(PostCommentMetric postCommentMetric) {
        return postCommentMetricJpaRepository.save(postCommentMetric);
    }
    
    @Override
    public Optional<PostCommentMetric> findById(Long id) {
        return postCommentMetricJpaRepository.findById(id);
    }
    
    @Override
    public List<PostCommentMetric> findByPostId(Long postId) {
        return postCommentMetricJpaRepository.findByPostId(postId);
    }
    
    @Override
    public List<PostCommentMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate) {
        return postCommentMetricJpaRepository.findByPostIdAndCrawledAtBetween(postId, startDate, endDate);
    }
    
    @Override
    public Optional<PostCommentMetric> findBySnsCommentId(String snsCommentId) {
        return postCommentMetricJpaRepository.findBySnsCommentId(snsCommentId);
    }
    
    @Override
    public void deleteByPostId(Long postId) {
        postCommentMetricJpaRepository.deleteByPostId(postId);
    }
    
    @Override
    public void deleteBySnsCommentId(String snsCommentId) {
        postCommentMetricJpaRepository.deleteBySnsCommentId(snsCommentId);
    }
}
