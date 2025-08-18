package kt.aivle.analytics.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoStatistics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeApiService {
    
    @Value("${app.youtube.api.key}")
    private String apiKey;
    
    private final YouTubeApiQuotaManager quotaManager;
    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final AnalyticsCacheService cacheService;
    private YouTube youtubeClient;
    
    /**
     * 실시간 게시물 메트릭을 조회합니다.
     */
    public List<RealtimePostMetricsResponse> getRealtimePostMetrics(Long postId) {
        // 캐시에서 먼저 확인
        var cached = cacheService.getCachedRealtimePostMetrics(postId);
        if (cached.isPresent()) {
            log.debug("Returning cached realtime post metrics for postId: {}", postId);
            return cached.get();
        }
        
        try {
            SnsPost post = snsPostRepositoryPort.findById(postId)
                .orElseThrow(() -> new AnalyticsException(AnalyticsErrorCode.POST_NOT_FOUND, "Post not found: " + postId));
            
            VideoStatistics statistics = getVideoStatistics(post.getSnsPostId());
            
            if (statistics != null) {
                RealtimePostMetricsResponse response = RealtimePostMetricsResponse.builder()
                    .postId(postId)
                    .snsPostId(post.getSnsPostId())
                    .accountId(post.getAccountId())
                    .likes(statistics.getLikeCount() != null ? statistics.getLikeCount().toString() : "0")
                    .dislikes(statistics.getDislikeCount() != null ? statistics.getDislikeCount().longValue() : 0L)
                    .comments(statistics.getCommentCount() != null ? statistics.getCommentCount().longValue() : 0L)
                    .shares(null) // YouTube API v3에서는 공유 수를 직접 제공하지 않음
                    .views(statistics.getViewCount() != null ? statistics.getViewCount().longValue() : 0L)
                    .fetchedAt(LocalDateTime.now())
                    .dataSource("youtube_api")
                    .isCached(false)
                    .build();
                
                List<RealtimePostMetricsResponse> result = List.of(response);
                
                // 캐시에 저장
                cacheService.cacheRealtimePostMetrics(postId, result);
                
                return result;
            }
            
            return List.of();
            
        } catch (AnalyticsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting realtime post metrics for postId: {}", postId, e);
            throw new AnalyticsException(AnalyticsErrorCode.YOUTUBE_API_ERROR, "Failed to get realtime post metrics", e);
        }
    }
    
    /**
     * 실시간 계정 메트릭을 조회합니다.
     */
    public List<RealtimeAccountMetricsResponse> getRealtimeAccountMetrics(Long accountId) {
        // 캐시에서 먼저 확인
        var cached = cacheService.getCachedRealtimeAccountMetrics(accountId);
        if (cached.isPresent()) {
            log.debug("Returning cached realtime account metrics for accountId: {}", accountId);
            return cached.get();
        }
        
        try {
            SnsAccount account = snsAccountRepositoryPort.findById(accountId)
                .orElseThrow(() -> new AnalyticsException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND, "Account not found: " + accountId));
            
            ChannelStatistics statistics = getChannelStatistics(account.getSnsAccountId());
            
            if (statistics != null) {
                RealtimeAccountMetricsResponse response = RealtimeAccountMetricsResponse.builder()
                    .accountId(accountId)
                    .snsAccountId(account.getSnsAccountId())
                    .followers(statistics.getSubscriberCount() != null ? statistics.getSubscriberCount().longValue() : 0L)
                    .views(statistics.getViewCount() != null ? statistics.getViewCount().longValue() : 0L)
                    .fetchedAt(LocalDateTime.now())
                    .dataSource("youtube_api")
                    .isCached(false)
                    .build();
                
                List<RealtimeAccountMetricsResponse> result = List.of(response);
                
                // 캐시에 저장
                cacheService.cacheRealtimeAccountMetrics(accountId, result);
                
                return result;
            }
            
            return List.of();
            
        } catch (AnalyticsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting realtime account metrics for accountId: {}", accountId, e);
            throw new AnalyticsException(AnalyticsErrorCode.YOUTUBE_API_ERROR, "Failed to get realtime account metrics", e);
        }
    }
    
    /**
     * 실시간 게시물 댓글을 조회합니다.
     */
    public List<PostCommentsQueryResponse> getRealtimePostComments(Long postId, Integer page, Integer size) {
        // 캐시에서 먼저 확인
        var cached = cacheService.getCachedRealtimePostComments(postId, page, size);
        if (cached.isPresent()) {
            log.debug("Returning cached realtime post comments for postId: {}, page: {}, size: {}", postId, page, size);
            return cached.get();
        }
        
        try {
            SnsPost post = snsPostRepositoryPort.findById(postId)
                .orElseThrow(() -> new AnalyticsException(AnalyticsErrorCode.POST_NOT_FOUND, "Post not found: " + postId));
            
            YouTube youtube = getYouTubeClient();
            
            CommentThreadListResponse response = youtube.commentThreads()
                .list(List.of("snippet"))
                .setVideoId(post.getSnsPostId())
                .setMaxResults((long) size)
                .setOrder("time") // 최신순 정렬
                .setKey(apiKey)
                .execute();
                
            quotaManager.incrementApiCall();
            
            if (response.getItems() != null) {
                List<PostCommentsQueryResponse> result = response.getItems().stream()
                    .map(commentThread -> {
                        var comment = commentThread.getSnippet().getTopLevelComment();
                        return PostCommentsQueryResponse.builder()
                            .commentId(comment.getId())
                            .authorName(comment.getSnippet().getAuthorDisplayName())
                            .text(comment.getSnippet().getTextDisplay())
                            .likeCount(comment.getSnippet().getLikeCount() != null ? comment.getSnippet().getLikeCount().longValue() : 0L)
                            .publishedAt(LocalDateTime.now()) // 임시로 현재 시간 사용
                            .crawledAt(LocalDateTime.now())
                            .build();
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                // 캐시에 저장
                cacheService.cacheRealtimePostComments(postId, page, size, result);
                
                return result;
            }
            
            return List.of();
            
        } catch (AnalyticsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting realtime post comments for postId: {}", postId, e);
            throw new AnalyticsException(AnalyticsErrorCode.YOUTUBE_API_ERROR, "Failed to get realtime post comments", e);
        }
    }
    
    /**
     * 채널 통계 정보를 조회합니다.
     */
    public ChannelStatistics getChannelStatistics(String channelId) {
        try {
            YouTube youtube = getYouTubeClient();
            
            ChannelListResponse response = youtube.channels()
                .list(List.of("statistics"))
                .setId(List.of(channelId))
                .setKey(apiKey)
                .execute();
                
            quotaManager.incrementApiCall();
            
            if (response.getItems() != null && !response.getItems().isEmpty()) {
                return response.getItems().get(0).getStatistics();
            }
            
            log.warn("No channel found for channelId: {}", channelId);
            return null;
            
        } catch (IOException e) {
            log.error("Failed to get channel statistics for channelId: {}", channelId, e);
            throw new AnalyticsException(AnalyticsErrorCode.YOUTUBE_API_ERROR, "Failed to get channel statistics", e);
        }
    }
    
    /**
     * 비디오 통계 정보를 조회합니다.
     */
    public VideoStatistics getVideoStatistics(String videoId) {
        try {
            YouTube youtube = getYouTubeClient();
            
            VideoListResponse response = youtube.videos()
                .list(List.of("statistics"))
                .setId(List.of(videoId))
                .setKey(apiKey)
                .execute();
                
            quotaManager.incrementApiCall();
            
            if (response.getItems() != null && !response.getItems().isEmpty()) {
                return response.getItems().get(0).getStatistics();
            }
            
            log.warn("No video found for videoId: {}", videoId);
            return null;
            
        } catch (IOException e) {
            log.error("Failed to get video statistics for videoId: {}", videoId, e);
            throw new AnalyticsException(AnalyticsErrorCode.YOUTUBE_API_ERROR, "Failed to get video statistics", e);
        }
    }
    
    /**
     * API 키를 반환합니다.
     */
    public String getApiKey() {
        return apiKey;
    }
    
    /**
     * YouTube 클라이언트를 싱글톤으로 생성합니다.
     */
    private synchronized YouTube getYouTubeClient() {
        if (youtubeClient == null) {
            youtubeClient = new YouTube.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                null
            ).build();
        }
        return youtubeClient;
    }
}
