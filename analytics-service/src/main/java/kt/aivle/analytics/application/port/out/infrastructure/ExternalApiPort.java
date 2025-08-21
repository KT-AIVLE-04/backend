package kt.aivle.analytics.application.port.out.infrastructure;

import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;

/**
 * 외부 API 통신을 위한 Port 인터페이스
 * YouTube API 등 외부 시스템과의 통신을 추상화
 */
public interface ExternalApiPort {
    
    /**
     * YouTube 채널 통계 조회
     */
    ChannelStatistics getChannelStatistics(String channelId);
    
    /**
     * YouTube 비디오 통계 조회
     */
    VideoStatistics getVideoStatistics(String videoId);
    
    /**
     * YouTube 비디오 댓글 조회
     */
    List<PostCommentsQueryResponse> getVideoComments(String videoId);
    
    /**
     * 실시간 게시물 메트릭 조회
     */
    List<RealtimePostMetricsResponse> getRealtimePostMetrics(Long postId);
    
    /**
     * 실시간 계정 메트릭 조회
     */
    List<RealtimeAccountMetricsResponse> getRealtimeAccountMetrics(Long accountId);
    
    /**
     * YouTube 채널 통계 DTO
     */
    class ChannelStatistics {
        private final Long subscriberCount;
        private final Long viewCount;
        
        public ChannelStatistics(Long subscriberCount, Long viewCount) {
            this.subscriberCount = subscriberCount;
            this.viewCount = viewCount;
        }
        
        public Long getSubscriberCount() { return subscriberCount; }
        public Long getViewCount() { return viewCount; }
    }
    
    /**
     * YouTube 비디오 통계 DTO
     */
    class VideoStatistics {
        private final Long viewCount;
        private final Long likeCount;
        private final Long commentCount;
        
        public VideoStatistics(Long viewCount, Long likeCount, Long commentCount) {
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
        }
        
        public Long getViewCount() { return viewCount; }
        public Long getLikeCount() { return likeCount; }
        public Long getCommentCount() { return commentCount; }
    }
}
