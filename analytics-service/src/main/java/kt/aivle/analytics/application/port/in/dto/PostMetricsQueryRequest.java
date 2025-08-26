package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class PostMetricsQueryRequest extends BaseQueryRequest {
    
    private Long postId; // 로컬 DB의 sns_post 테이블 PK, null이면 모든 게시물
    
    private Long accountId; // SNS 계정 ID
    
    // 편의 메서드들
    public static PostMetricsQueryRequest forCurrentDate(Long postId, Long accountId) {
        return new PostMetricsQueryRequest(null, postId, accountId);
    }
    
    public static PostMetricsQueryRequest forDate(LocalDate date, Long postId, Long accountId) {
        return new PostMetricsQueryRequest(date, postId, accountId);
    }
    
    public static PostMetricsQueryRequest forLatestPostByAccountId(Long accountId) {
        return new PostMetricsQueryRequest(null, null, accountId);
    }
    
    public static PostMetricsQueryRequest forLatestPostByAccountId(LocalDate date, Long accountId) {
        return new PostMetricsQueryRequest(date, null, accountId);
    }
    
    // 생성자
    public PostMetricsQueryRequest(LocalDate date, Long postId, Long accountId) {
        super(date);
        this.postId = postId;
        this.accountId = accountId;
    }
}
