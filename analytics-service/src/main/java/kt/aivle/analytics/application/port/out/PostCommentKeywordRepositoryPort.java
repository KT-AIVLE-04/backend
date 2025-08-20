package kt.aivle.analytics.application.port.out;

import java.util.List;

import kt.aivle.analytics.domain.entity.PostCommentKeyword;

public interface PostCommentKeywordRepositoryPort {
    List<PostCommentKeyword> findByPostId(Long postId);
    void saveAll(List<PostCommentKeyword> keywords);
    void deleteByPostId(Long postId);
}
