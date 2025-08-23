package kt.aivle.analytics.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsPostJpaRepository;
import kt.aivle.analytics.application.port.out.repository.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPost;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SnsPostRepository implements SnsPostRepositoryPort {

    private final SnsPostJpaRepository snsPostJpaRepository;

    @Override
    public SnsPost save(SnsPost snsPost) {
        return snsPostJpaRepository.save(snsPost);
    }

    @Override
    public Optional<SnsPost> findById(Long id) {
        return snsPostJpaRepository.findById(id);
    }

    @Override
    public long countAll() {
        return snsPostJpaRepository.count();
    }

    @Override
    public List<SnsPost> findByAccountId(Long accountId) {
        return snsPostJpaRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<SnsPost> findBySnsPostId(String snsPostId) {
        return snsPostJpaRepository.findBySnsPostId(snsPostId);
    }

    @Override
    public void deleteById(Long id) {
        snsPostJpaRepository.deleteById(id);
    }

    @Override
    public List<SnsPost> findAllWithPagination(int page, int size) {
        return snsPostJpaRepository.findAllWithPagination(PageRequest.of(page, size));
    }
    
    @Override
    public List<SnsPost> findAllById(Set<Long> ids) {
        return snsPostJpaRepository.findAllById(ids);
    }
    
    @Override
    public Optional<SnsPost> findLatestByAccountId(Long accountId) {
        return snsPostJpaRepository.findLatestByAccountId(accountId);
    }
}
