package kt.aivle.analytics.adapter.out.infrastructure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.application.port.out.infrastructure.ExternalApiPort;
import kt.aivle.analytics.application.port.out.repository.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.domain.model.SnsType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeApiAdapter implements ExternalApiPort {
    
    @Value("${app.youtube.api.key}")
    private String apiKey;
    
    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private YouTube youtubeClient;
    
    @Override
    public ChannelStatistics getChannelStatistics(String channelId) {
        try {
            YouTube.Channels.List request = getYouTubeClient().channels()
                .list(List.of("statistics"))
                .setKey(apiKey)
                .setId(List.of(channelId));
            
            ChannelListResponse response = request.execute();
            
            if (response.getItems() == null || response.getItems().isEmpty()) {
                log.warn("Channel not found: {}", channelId);
                return null;
            }
            
            com.google.api.services.youtube.model.ChannelStatistics statistics = response.getItems().get(0).getStatistics();
            
            return new ChannelStatistics(
                statistics.getSubscriberCount() != null ? statistics.getSubscriberCount().longValue() : 0L,
                statistics.getViewCount() != null ? statistics.getViewCount().longValue() : 0L
            );
            
        } catch (IOException e) {
            handleYouTubeApiError(e, "channel statistics");
            return null;
        }
    }
    
    @Override
    public VideoStatistics getVideoStatistics(String videoId) {
        try {
            YouTube.Videos.List request = getYouTubeClient().videos()
                .list(List.of("statistics"))
                .setKey(apiKey)
                .setId(List.of(videoId));
            
            VideoListResponse response = request.execute();
            
            if (response.getItems() == null || response.getItems().isEmpty()) {
                log.warn("Video not found: {}", videoId);
                return null;
            }
            
            com.google.api.services.youtube.model.VideoStatistics statistics = response.getItems().get(0).getStatistics();
            
            return new VideoStatistics(
                statistics.getViewCount() != null ? statistics.getViewCount().longValue() : 0L,
                statistics.getLikeCount() != null ? statistics.getLikeCount().longValue() : 0L,
                statistics.getCommentCount() != null ? statistics.getCommentCount().longValue() : 0L
            );
            
        } catch (IOException e) {
            handleYouTubeApiError(e, "video statistics");
            return null;
        }
    }
    
    @Override
    public List<PostCommentsResponse> getVideoComments(String videoId) {
        try {
            YouTube.CommentThreads.List request = getYouTubeClient().commentThreads()
                .list(List.of("snippet"))
                .setKey(apiKey)
                .setVideoId(videoId)
                .setMaxResults(100L);
            
            CommentThreadListResponse response = request.execute();
            
            if (response.getItems() == null) {
                return List.of();
            }
            
            return response.getItems().stream()
                .map(this::toPostCommentsResponse)
                .collect(Collectors.toList());
            
        } catch (IOException e) {
            handleYouTubeApiError(e, "video comments");
            return List.of();
        }
    }
    
    @Override
    public List<PostCommentsResponse> getVideoCommentsWithPagination(String videoId, String pageToken) {
        try {
            YouTube.CommentThreads.List request = getYouTubeClient().commentThreads()
                .list(List.of("snippet"))
                .setKey(apiKey)
                .setVideoId(videoId)
                .setMaxResults(100L);
            
            if (pageToken != null) {
                request.setPageToken(pageToken);
            }
            
            CommentThreadListResponse response = request.execute();
            
            if (response.getItems() == null) {
                return List.of();
            }
            
            return response.getItems().stream()
                .map(this::toPostCommentsResponse)
                .collect(Collectors.toList());
            
        } catch (IOException e) {
            handleYouTubeApiError(e, "video comments with pagination");
            return List.of();
        }
    }
    
    @Override
    public String getNextPageToken(String videoId, String currentPageToken) {
        try {
            YouTube.CommentThreads.List request = getYouTubeClient().commentThreads()
                .list(List.of("snippet"))
                .setKey(apiKey)
                .setVideoId(videoId)
                .setMaxResults(100L);
            
            if (currentPageToken != null) {
                request.setPageToken(currentPageToken);
            }
            
            CommentThreadListResponse response = request.execute();
            return response.getNextPageToken();
            
        } catch (IOException e) {
            handleYouTubeApiError(e, "get next page token");
            return null;
        }
    }
    
    // AI 분석은 별도 AiAnalysisAdapter에서 처리
    
    @Override
    public List<PostMetricsResponse> getRealtimePostMetrics(Long postId) {
        try {
            SnsPost post = snsPostRepositoryPort.findById(postId)
                .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
            
            // SNS 타입별 처리 - accountId로 SnsAccount 조회
            SnsAccount account = snsAccountRepositoryPort.findById(post.getAccountId())
                .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND));
            SnsType snsType = account.getType();
            
            switch (snsType) {
                case youtube:
                    return getYouTubePostMetrics(postId, post);
                default:
                    log.warn("Unsupported SNS type: {} for postId: {}", snsType, postId);
                    return List.of();
            }
            
        } catch (Exception e) {
            log.error("Failed to get realtime post metrics for postId: {}", postId, e);
            return List.of();
        }
    }
    
    private List<PostMetricsResponse> getYouTubePostMetrics(Long postId, SnsPost post) {
        VideoStatistics statistics = getVideoStatistics(post.getSnsPostId());
        
        if (statistics != null) {
            PostMetricsResponse response = PostMetricsResponse.builder()
                .postId(postId)
                .accountId(post.getAccountId())
                .views(statistics.viewCount())
                .likes(statistics.likeCount())
                .comments(statistics.commentCount())
                .fetchedAt(LocalDateTime.now())
                .snsType(SnsType.youtube)
                .isCached(false)
                .build();
            
            return List.of(response);
        }
        
        return List.of();
    }
    

    
    @Override
    public List<AccountMetricsResponse> getRealtimeAccountMetrics(Long accountId) {
        try {
            SnsAccount account = snsAccountRepositoryPort.findById(accountId)
                .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND));
            
            // SNS 타입별 처리
            SnsType snsType = account.getType();
            
            switch (snsType) {
                case youtube:
                    return getYouTubeAccountMetrics(accountId, account);
                default:
                    log.warn("Unsupported SNS type: {} for accountId: {}", snsType, accountId);
                    return List.of();
            }
            
        } catch (Exception e) {
            log.error("Failed to get realtime account metrics for accountId: {}", accountId, e);
            return List.of();
        }
    }
    
    private List<AccountMetricsResponse> getYouTubeAccountMetrics(Long accountId, SnsAccount account) {
        ChannelStatistics statistics = getChannelStatistics(account.getSnsAccountId());
        
        if (statistics != null) {
            AccountMetricsResponse response = AccountMetricsResponse.builder()
                .accountId(accountId)
                .followers(statistics.getSubscriberCount())
                .views(statistics.getViewCount())
                .fetchedAt(LocalDateTime.now())
                .snsType(SnsType.youtube)
                .isCached(false)
                .build();
            
            return List.of(response);
        }
        
        return List.of();
    }
    

    
    private PostCommentsResponse toPostCommentsResponse(CommentThread commentThread) {
        var snippet = commentThread.getSnippet().getTopLevelComment().getSnippet();
        
        return PostCommentsResponse.builder()
            .commentId(commentThread.getId())  // YouTube comment ID
            .authorId(snippet.getAuthorChannelId() != null ? snippet.getAuthorChannelId().getValue() : null)
            .text(snippet.getTextDisplay())
            .likeCount(snippet.getLikeCount() != null ? snippet.getLikeCount().longValue() : 0L)
            .publishedAt(ZonedDateTime.parse(snippet.getPublishedAt().toString()).toLocalDateTime())
            .build();
    }
    
    private void handleYouTubeApiError(IOException e, String operation) {
        if (e instanceof GoogleJsonResponseException) {
            GoogleJsonResponseException googleError = (GoogleJsonResponseException) e;
            int statusCode = googleError.getStatusCode();
            
            log.error("YouTube API error - Status: {}, Operation: {}", statusCode, operation);
            
            if (statusCode == 403 || statusCode == 429) {
                String errorMessage = googleError.getDetails() != null ? 
                    googleError.getDetails().getMessage() : "Quota exceeded";
                
                if (errorMessage.toLowerCase().contains("quota") || 
                    errorMessage.toLowerCase().contains("limit") ||
                    errorMessage.toLowerCase().contains("exceeded")) {
                    
                    log.warn("YouTube API quota exceeded detected: {}", errorMessage);
                    throw new BusinessException(AnalyticsErrorCode.YOUTUBE_QUOTA_EXCEEDED);
                }
            }
            
            if (statusCode == 404) {
                if (operation.contains("video")) {
                    throw new BusinessException(AnalyticsErrorCode.YOUTUBE_VIDEO_NOT_FOUND);
                } else if (operation.contains("channel")) {
                    throw new BusinessException(AnalyticsErrorCode.YOUTUBE_CHANNEL_NOT_FOUND);
                }
            }
        }
        
        throw new BusinessException(AnalyticsErrorCode.YOUTUBE_API_ERROR);
    }
    
    private YouTube getYouTubeClient() {
        if (youtubeClient == null) {
            try {
                youtubeClient = new YouTube.Builder(
                                    GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("Analytics Service")
                    .build();
            } catch (Exception e) {
                log.error("Failed to create YouTube client", e);
                throw new RuntimeException("Failed to create YouTube client", e);
            }
        }
        return youtubeClient;
    }
}
