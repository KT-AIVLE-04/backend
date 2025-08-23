package kt.aivle.analytics.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import kt.aivle.analytics.domain.entity.SnsPost;

public interface SnsPostRepositoryPort {
    SnsPost save(SnsPost snsPost);
    Optional<SnsPost> findById(Long id);
    List<SnsPost> findAll();
    long countAll();
    List<SnsPost> findByAccountId(Long accountId);
    Optional<SnsPost> findBySnsPostId(String snsPostId);
    void deleteById(Long id);
    List<SnsPost> findAllWithPagination(int page, int size);
    List<SnsPost> findAllById(Set<Long> ids);
    Optional<SnsPost> findLatestByAccountId(Long accountId);  // 최근 게시물 1개만 조회
}
