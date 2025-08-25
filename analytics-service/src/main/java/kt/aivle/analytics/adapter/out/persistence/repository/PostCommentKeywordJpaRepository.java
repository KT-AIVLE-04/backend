package kt.aivle.analytics.adapter.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.PostCommentKeyword;

@Repository
public interface PostCommentKeywordJpaRepository extends BaseJpaRepository<PostCommentKeyword, Long> {
    
    List<PostCommentKeyword> findByPostId(Long postId);
    
    @Modifying
    @Query("DELETE FROM post_comment_keyword p WHERE p.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
