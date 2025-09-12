package kt.aivle.analytics.application.port.in.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class PostCommentsQueryRequest {
    
    private Long userId; // 사용자 ID
    
    private Long postId; // null이면 모든 게시물
    
    /**
     * YouTube API 페이지네이션 토큰 (첫 페이지는 null)
     */
    private String pageToken;
    
    @Min(value = 1, message = "Size must be 1 or greater")
    @Max(value = 100, message = "Size must be 100 or less")
    @Builder.Default
    private Integer size = 20; // 기본값 20
    
    private Long accountId; // 계정 ID

    public static PostCommentsQueryRequest forPost(Long userId, Long postId, String pageToken, Integer size, Long accountId) {
        return new PostCommentsQueryRequest(userId, postId, pageToken, size, accountId);
    }
    
    public static PostCommentsQueryRequest forLatestPostByAccountId(Long userId, Long accountId, String pageToken, Integer size) {
        return new PostCommentsQueryRequest(userId, null, pageToken, size, accountId);
    }

    public PostCommentsQueryRequest(Long userId, Long postId, String pageToken, Integer size, Long accountId) {
        this.userId = userId;
        this.postId = postId;
        this.pageToken = pageToken;
        this.size = size != null ? size : 20;
        this.accountId = accountId;
    }
}
