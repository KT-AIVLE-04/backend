package kt.aivle.analytics.application.port.out;

import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.Post;

public interface PostRepositoryPort {
    
    Post save(Post post);
    
    Optional<Post> findById(Long id);
    
    Optional<Post> findBySnsPostId(String snsPostId);
    
    List<Post> findByAccountId(Long accountId);
    
    List<Post> findAll();
    
    List<Post> findAllWithPagination(int page, int size);
    
    void deleteById(Long id);
    
    void deleteBySnsPostId(String snsPostId);
}
