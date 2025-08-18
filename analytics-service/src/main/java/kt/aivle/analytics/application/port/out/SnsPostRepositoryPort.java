package kt.aivle.analytics.application.port.out;

import kt.aivle.analytics.domain.entity.SnsPost;

import java.util.List;
import java.util.Optional;

public interface SnsPostRepositoryPort {
    SnsPost save(SnsPost snsPost);
    Optional<SnsPost> findById(Long id);
    List<SnsPost> findAll();
    long countAll();
    List<SnsPost> findByAccountId(Long accountId);
    Optional<SnsPost> findBySnsPostId(String snsPostId);
    void deleteById(Long id);
    List<SnsPost> findAllWithPagination(int page, int size);
}
