package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.Comment;
import kt.aivle.analytics.domain.port.out.CommentRepositoryPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentRepository implements CommentRepositoryPort {
    
    private final CommentJpaRepository jpaRepository;
    
    @Override
    public List<Comment> findByUserIdAndSocialPostId(String userId, Long socialPostId) {
        return jpaRepository.findByUserIdAndSocialPostIdOrderByCrawledAtDesc(userId, socialPostId);
    }
    
    @Override
    public List<Comment> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByUserIdAndCrawledAtBetweenOrderByCrawledAtDesc(userId, startDate, endDate);
    }
    
    @Override
    public List<Comment> findBySocialPostId(Long socialPostId) {
        return jpaRepository.findBySocialPostId(socialPostId);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
    
    @Override
    public Comment save(Comment comment) {
        return jpaRepository.save(comment);
    }
    
    @Override
    public List<Comment> saveAll(List<Comment> comments) {
        return jpaRepository.saveAll(comments);
    }
}
