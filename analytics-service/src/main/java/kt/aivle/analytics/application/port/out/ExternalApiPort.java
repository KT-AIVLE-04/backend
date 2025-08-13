package kt.aivle.analytics.application.port.out;

import java.util.List;
import java.util.Map;

public interface ExternalApiPort {
    
    // YouTube API
    List<VideoData> getYouTubeVideos(String accessToken, String channelId);
    
    VideoMetrics getYouTubeVideoMetrics(String accessToken, String videoId);
    
    ChannelData getYouTubeChannelData(String accessToken, String channelId);
    
    List<CommentData> getYouTubeComments(String accessToken, String videoId);
    
    // AI 서버
    SentimentAnalysisResult analyzeSentiment(String videoId, List<String> comments);
    
    TrendAnalysisResult analyzeTrends(String userId, Map<String, Object> metrics);
    
    OptimalTimeResult analyzeOptimalPostingTime(String userId, Map<String, Object> postingData);
    
    // 토큰 갱신
    TokenRefreshResult refreshToken(String refreshToken, String snsType);
    
    record VideoData(String videoId, String title, String description, String publishedAt) {}
    
    record VideoMetrics(Long viewCount, Long likeCount, Long commentCount, Long shareCount) {}
    
    record ChannelData(String channelId, String channelName, Long subscriberCount, Long videoCount) {}
    
    record CommentData(String commentId, String content, String authorName, String publishedAt) {}
    
    record SentimentAnalysisResult(Double positiveScore, Double negativeScore, Double neutralScore, String summary) {}
    
    record TrendAnalysisResult(String trend, Double confidence, String recommendation) {}
    
    record OptimalTimeResult(String optimalDay, String optimalTime, Double expectedEngagement) {}
    
    record TokenRefreshResult(String accessToken, String refreshToken, Long expiresAt) {}
}
