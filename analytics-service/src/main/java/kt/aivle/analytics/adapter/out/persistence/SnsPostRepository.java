package kt.aivle.analytics.adapter.out.persistence;

import kt.aivle.analytics.application.port.out.repository.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.adapter.out.persistence.repository.SnsPostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public List<SnsPost> findAll() {
        return snsPostJpaRepository.findAll();
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
}
