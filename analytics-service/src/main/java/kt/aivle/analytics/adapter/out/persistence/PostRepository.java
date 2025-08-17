package kt.aivle.analytics.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.PostJpaRepository;
import kt.aivle.analytics.application.port.out.PostRepositoryPort;
import kt.aivle.analytics.domain.entity.Post;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostRepository implements PostRepositoryPort {
    
    private final PostJpaRepository postJpaRepository;
    
    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }
    
    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id);
    }
    
    @Override
    public Optional<Post> findBySnsPostId(String snsPostId) {
        return postJpaRepository.findBySnsPostId(snsPostId);
    }
    
    @Override
    public List<Post> findByAccountId(Long accountId) {
        return postJpaRepository.findByAccountId(accountId);
    }
    
    @Override
    public List<Post> findAll() {
        return postJpaRepository.findAll();
    }
    
    @Override
    public List<Post> findAllWithPagination(int page, int size) {
        return postJpaRepository.findAllWithPagination(page, size);
    }
    
    @Override
    public void deleteById(Long id) {
        postJpaRepository.deleteById(id);
    }
    
    @Override
    public void deleteBySnsPostId(String snsPostId) {
        postJpaRepository.deleteBySnsPostId(snsPostId);
    }
}
