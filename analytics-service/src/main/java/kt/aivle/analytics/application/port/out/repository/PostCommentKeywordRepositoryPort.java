package kt.aivle.analytics.application.port.out.repository;

import java.util.List;
import java.util.Map;

import kt.aivle.analytics.domain.entity.PostCommentKeyword;
import kt.aivle.analytics.domain.model.SentimentType;

public interface PostCommentKeywordRepositoryPort {
    List<PostCommentKeyword> findByPostId(Long postId);
    Map<SentimentType, List<String>> findKeywordsByPostIdGroupedBySentiment(Long postId);
    void saveAll(List<PostCommentKeyword> keywords);
    void deleteByPostId(Long postId);
}
