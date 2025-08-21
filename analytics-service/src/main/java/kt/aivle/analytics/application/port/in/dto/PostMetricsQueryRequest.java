package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class PostMetricsQueryRequest extends BaseQueryRequest {
    
    @Pattern(regexp = "^\\d+$", message = "AccountId must be a valid number")
    private String accountId; // null이면 모든 계정
    
    @Pattern(regexp = "^\\d+$", message = "PostId must be a valid number")
    private String postId; // null이면 모든 게시물
    
    // 편의 메서드들
    public static PostMetricsQueryRequest forCurrentDate(String postId) {
        return new PostMetricsQueryRequest(null, null, postId);
    }
    
    public static PostMetricsQueryRequest forDate(LocalDate date, String accountId, String postId) {
        return new PostMetricsQueryRequest(date, accountId, postId);
    }
    
    public static PostMetricsQueryRequest forAccount(LocalDate date, String accountId) {
        return new PostMetricsQueryRequest(date, accountId, null);
    }
    
    public static PostMetricsQueryRequest forAllPosts(LocalDate date) {
        return new PostMetricsQueryRequest(date, null, null);
    }
    
    // 생성자
    public PostMetricsQueryRequest(LocalDate date, String accountId, String postId) {
        super(date);
        this.accountId = accountId;
        this.postId = postId;
    }
    
    // 검증 메서드
    public boolean isValidIds() {
        return (accountId == null || accountId.matches("\\d+")) &&
               (postId == null || postId.matches("\\d+"));
    }
}
