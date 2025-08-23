package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import kt.aivle.analytics.domain.model.SnsType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class PostCommentsQueryRequest extends BaseQueryRequest {
    
    @Pattern(regexp = "^\\d+$", message = "PostId must be a valid number")
    private String postId; // null이면 모든 게시물
    
    @Min(value = 0, message = "Page must be 0 or greater")
    @Builder.Default
    private Integer page = 0; // 기본값 0
    
    @Min(value = 1, message = "Size must be 1 or greater")
    @Max(value = 100, message = "Size must be 100 or less")
    @Builder.Default
    private Integer size = 20; // 기본값 20
    
    private SnsType snsType; // SNS 타입
    private String userId; // 사용자 ID

    public static PostCommentsQueryRequest forCurrentDate(String postId, Integer page, Integer size) {
        return new PostCommentsQueryRequest(null, postId, page, size, null, null);
    }
    public static PostCommentsQueryRequest forDate(LocalDate date, String postId, Integer page, Integer size) {
        return new PostCommentsQueryRequest(date, postId, page, size, null, null);
    }
    
    public static PostCommentsQueryRequest forLatestPostBySnsType(String userId, SnsType snsType, Integer page, Integer size) {
        return new PostCommentsQueryRequest(null, null, page, size, snsType, userId);
    }

    
    public PostCommentsQueryRequest(LocalDate date, String postId, Integer page, Integer size, SnsType snsType, String userId) {
        super(date);
        this.postId = postId;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
        this.snsType = snsType;
        this.userId = userId;
    }
}
