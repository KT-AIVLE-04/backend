package kt.aivle.analytics.adapter.in.web.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * YouTube API 페이지네이션을 지원하는 댓글 응답 DTO
 */
@Getter
@Builder
public class PostCommentsPageResponse {
    
    /**
     * 댓글 데이터 목록
     */
    private List<PostCommentsResponse> data;
    
    /**
     * 다음 페이지 요청용 토큰 (YouTube API)
     */
    private String nextPageToken;
    
    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNextPage;
    
    /**
     * 현재 페이지의 댓글 수
     */
    private int currentPageSize;
    
    /**
     * 빈 페이지 응답 생성
     */
    public static PostCommentsPageResponse empty() {
        return PostCommentsPageResponse.builder()
            .data(List.of())
            .nextPageToken(null)
            .hasNextPage(false)
            .currentPageSize(0)
            .build();
    }
}
