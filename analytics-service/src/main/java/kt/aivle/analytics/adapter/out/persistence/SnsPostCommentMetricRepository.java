package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsPostCommentMetricJpaRepository;
import kt.aivle.analytics.application.port.out.repository.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.model.SentimentType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SnsPostCommentMetricRepository implements SnsPostCommentMetricRepositoryPort {

    private final SnsPostCommentMetricJpaRepository snsPostCommentMetricJpaRepository;

    @Override
    public SnsPostCommentMetric save(SnsPostCommentMetric snsPostCommentMetric) {
        return snsPostCommentMetricJpaRepository.save(snsPostCommentMetric);
    }

    @Override
    public Optional<SnsPostCommentMetric> findById(Long id) {
        return snsPostCommentMetricJpaRepository.findById(id);
    }

    @Override
    public List<SnsPostCommentMetric> findByPostId(Long postId) {
        return snsPostCommentMetricJpaRepository.findByPostId(postId);
    }
    
    @Override
    public List<SnsPostCommentMetric> findByPostIdWithPagination(Long postId, int page, int size) {
        return snsPostCommentMetricJpaRepository.findByPostIdWithPagination(postId, PageRequest.of(page, size));
    }

    @Override
    public Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId) {
        return snsPostCommentMetricJpaRepository.findBySnsCommentId(snsCommentId);
    }

    @Override
    public void deleteById(Long id) {
        snsPostCommentMetricJpaRepository.deleteById(id);
    }
    
    @Override
    public List<SnsPostCommentMetric> findByPostIdAndCreatedAtDate(Long postId, LocalDate date) {
        // LocalDate를 직접 사용 (JpaRepository에서 타임존 처리)
        return snsPostCommentMetricJpaRepository.findByPostIdAndCreatedAtDate(postId, date);
    }
    
    @Override
    public List<SnsPostCommentMetric> findByPostIdAndPublishedAtBeforeWithPagination(Long postId, LocalDate date, int page, int size) {
        return snsPostCommentMetricJpaRepository.findByPostIdAndPublishedAtBeforeWithPagination(postId, date, PageRequest.of(page, size));
    }

    @Override
    public void saveAll(List<SnsPostCommentMetric> metrics) {
        snsPostCommentMetricJpaRepository.saveAll(metrics);
    }
    
    @Override
    public void updateSentimentById(Long id, SentimentType sentiment) {
        snsPostCommentMetricJpaRepository.updateSentimentById(id, sentiment);
    }
}
