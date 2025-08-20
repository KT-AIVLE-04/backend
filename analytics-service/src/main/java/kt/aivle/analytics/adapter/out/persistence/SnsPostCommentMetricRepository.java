package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsPostCommentMetricJpaRepository;
import kt.aivle.analytics.application.port.out.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
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
    public List<SnsPostCommentMetric> findAll() {
        return snsPostCommentMetricJpaRepository.findAll();
    }

    @Override
    public List<SnsPostCommentMetric> findByPostId(Long postId) {
        return snsPostCommentMetricJpaRepository.findByPostId(postId);
    }

    @Override
    public List<SnsPostCommentMetric> findByPostIdAndCreatedAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate) {
        return snsPostCommentMetricJpaRepository.findByPostIdAndCreatedAtBetween(postId, startDate, endDate);
    }

    @Override
    public List<SnsPostCommentMetric> findByPostIdAndCreatedAtBetweenWithPagination(Long postId, LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return snsPostCommentMetricJpaRepository.findByPostIdAndCreatedAtBetweenWithPagination(postId, startDate, endDate, pageRequest);
    }

    @Override
    public List<SnsPostCommentMetric> findByPostIdsAndCreatedAtBetweenWithPagination(List<Long> postIds, LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return snsPostCommentMetricJpaRepository.findByPostIdsAndCreatedAtBetweenWithPagination(postIds, startDate, endDate, pageRequest);
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
    public void saveAll(List<SnsPostCommentMetric> metrics) {
        snsPostCommentMetricJpaRepository.saveAll(metrics);
    }
}
