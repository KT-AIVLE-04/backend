package kt.aivle.analytics.application.port.out;

import java.util.List;

import kt.aivle.analytics.domain.entity.PostCommentKeyword;
import kt.aivle.analytics.domain.model.SentimentType;

public interface PostCommentKeywordRepositoryPort {
    List<PostCommentKeyword> findByPostId(Long postId);
    List<String> findKeywordsByPostIdAndSentiment(Long postId, SentimentType sentiment);
    void saveAll(List<PostCommentKeyword> keywords);
    void deleteByPostId(Long postId);
}
