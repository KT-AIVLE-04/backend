package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.Post;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostJpaRepository extends JpaRepository<Post, Long> {
    
    Optional<Post> findBySnsPostId(String snsPostId);
    
    List<Post> findByAccountId(Long accountId);
    
    @Query("SELECT p FROM Post p ORDER BY p.id")
    List<Post> findAllWithPagination(Pageable pageable);
    
    default List<Post> findAllWithPagination(int page, int size) {
        return findAllWithPagination(PageRequest.of(page, size));
    }
    
    void deleteBySnsPostId(String snsPostId);
}
