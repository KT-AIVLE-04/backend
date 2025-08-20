package kt.aivle.analytics.adapter.out.persistence;

import java.util.Date;
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
    public Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId) {
        return snsPostCommentMetricJpaRepository.findBySnsCommentId(snsCommentId);
    }

    @Override
    public void deleteById(Long id) {
        snsPostCommentMetricJpaRepository.deleteById(id);
    }
    
    @Override
    public List<SnsPostCommentMetric> findByPostIdAndCreatedAtDate(Long postId, Date date) {
        return snsPostCommentMetricJpaRepository.findByPostIdAndCreatedAtDate(postId, date);
    }

    @Override
    public List<SnsPostCommentMetric> findByPostIdAndCreatedAtDateWithPagination(Long postId, Date date, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return snsPostCommentMetricJpaRepository.findByPostIdAndCreatedAtDateWithPagination(postId, date, pageRequest);
    }

    @Override
    public void saveAll(List<SnsPostCommentMetric> metrics) {
        snsPostCommentMetricJpaRepository.saveAll(metrics);
    }
}
