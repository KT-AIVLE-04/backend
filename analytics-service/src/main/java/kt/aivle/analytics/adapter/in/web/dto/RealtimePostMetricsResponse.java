package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimePostMetricsResponse {
    private Long postId;           // Local DB ID
    private String snsPostId;      // YouTube Video ID
    private Long accountId;        // Local Account ID
    private String likes;          // 실시간 좋아요 수
    private Long dislikes;         // 실시간 싫어요 수
    private Long comments;         // 실시간 댓글 수
    private Long shares;           // 실시간 공유 수
    private Long views;            // 실시간 조회 수
    private LocalDateTime fetchedAt; // API 호출 시간
    private String dataSource;     // "youtube_api" 또는 "cache"
    private Boolean isCached;      // 캐시된 데이터인지 여부
}
