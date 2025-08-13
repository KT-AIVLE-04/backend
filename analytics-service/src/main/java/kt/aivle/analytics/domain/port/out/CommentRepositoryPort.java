package kt.aivle.analytics.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;

import kt.aivle.analytics.domain.entity.Comment;

public interface CommentRepositoryPort {
    
    List<Comment> findByUserIdAndSocialPostId(String userId, Long socialPostId);
    
    List<Comment> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Comment> findBySocialPostId(Long socialPostId);
    
    void deleteByUserId(String userId);
    
    Comment save(Comment comment);
    
    List<Comment> saveAll(List<Comment> comments);
}
