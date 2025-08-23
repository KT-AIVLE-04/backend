package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Pattern;
import kt.aivle.analytics.domain.model.SnsType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class PostMetricsQueryRequest extends BaseQueryRequest {
    
    @Pattern(regexp = "^\\d+$", message = "PostId must be a valid number")
    private String postId; // 로컬 DB의 sns_post 테이블 PK, null이면 모든 게시물
    
    private SnsType snsType; // SNS 타입
    private String userId; // 사용자 ID
    
    // 편의 메서드들
    public static PostMetricsQueryRequest forCurrentDate(String postId) {
        return new PostMetricsQueryRequest(null, postId, null, null);
    }
    
    public static PostMetricsQueryRequest forDate(LocalDate date, String postId) {
        return new PostMetricsQueryRequest(date, postId, null, null);
    }
    

    

    
    public static PostMetricsQueryRequest forLatestPostBySnsType(String userId, SnsType snsType) {
        return new PostMetricsQueryRequest(null, null, snsType, userId);
    }
    
    public static PostMetricsQueryRequest forLatestPostBySnsType(LocalDate date, String userId, SnsType snsType) {
        return new PostMetricsQueryRequest(date, null, snsType, userId);
    }
    
    // 생성자
    public PostMetricsQueryRequest(LocalDate date, String postId, SnsType snsType, String userId) {
        super(date);
        this.postId = postId;
        this.snsType = snsType;
        this.userId = userId;
    }
}
