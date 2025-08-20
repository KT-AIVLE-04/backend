package kt.aivle.analytics.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.PostCommentKeywordJpaRepository;
import kt.aivle.analytics.application.port.out.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.domain.entity.PostCommentKeyword;
import kt.aivle.analytics.domain.model.SentimentType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostCommentKeywordRepository implements PostCommentKeywordRepositoryPort {
    
    private final PostCommentKeywordJpaRepository postCommentKeywordJpaRepository;
    
    @Override
    public List<PostCommentKeyword> findByPostId(Long postId) {
        return postCommentKeywordJpaRepository.findByPostId(postId);
    }
    
    @Override
    public List<String> findKeywordsByPostIdAndSentiment(Long postId, SentimentType sentiment) {
        return postCommentKeywordJpaRepository.findByPostIdAndSentiment(postId, sentiment)
            .stream()
            .map(PostCommentKeyword::getKeyword)
            .toList();
    }
    
    @Override
    public void saveAll(List<PostCommentKeyword> keywords) {
        postCommentKeywordJpaRepository.saveAll(keywords);
    }
    
    @Override
    public void deleteByPostId(Long postId) {
        postCommentKeywordJpaRepository.deleteByPostId(postId);
    }
}
