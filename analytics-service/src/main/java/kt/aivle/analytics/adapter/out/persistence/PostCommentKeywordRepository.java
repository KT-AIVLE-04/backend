package kt.aivle.analytics.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.PostCommentKeywordJpaRepository;
import kt.aivle.analytics.application.port.out.repository.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.domain.entity.PostCommentKeyword;
import kt.aivle.analytics.domain.model.SentimentType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostCommentKeywordRepository implements PostCommentKeywordRepositoryPort {
    
    private final PostCommentKeywordJpaRepository postCommentKeywordJpaRepository;

    
    @Override
    public Map<SentimentType, List<String>> findKeywordsByPostIdGroupedBySentiment(Long postId) {
        List<PostCommentKeyword> keywords = postCommentKeywordJpaRepository.findByPostId(postId);
        
        return keywords.stream()
            .collect(Collectors.groupingBy(
                PostCommentKeyword::getSentiment,
                Collectors.mapping(PostCommentKeyword::getKeyword, Collectors.toList())
            ));
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
