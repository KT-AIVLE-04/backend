package kt.aivle.analytics.application.service;

import java.io.IOException;
import java.util.Arrays;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoStatistics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.exception.AnalyticsException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class YouTubeApiService {
    
    @Value("${app.youtube.api.key}")
    private String apiKey;
    
    private final YouTubeApiQuotaManager quotaManager;
    private YouTube youtubeClient;
    
    public YouTubeApiService(YouTubeApiQuotaManager quotaManager) {
        this.quotaManager = quotaManager;
    }
    
    /**
     * 채널 통계 정보를 조회합니다.
     */
    public ChannelStatistics getChannelStatistics(String channelId) {
        try {
            YouTube youtube = getYouTubeClient();
            
            ChannelListResponse response = youtube.channels()
                .list(Arrays.asList("statistics"))
                .setId(Arrays.asList(channelId))
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
            throw new AnalyticsException("Failed to get channel statistics", e);
        }
    }
    
    /**
     * 비디오 통계 정보를 조회합니다.
     */
    public VideoStatistics getVideoStatistics(String videoId) {
        try {
            YouTube youtube = getYouTubeClient();
            
            VideoListResponse response = youtube.videos()
                .list(Arrays.asList("statistics"))
                .setId(Arrays.asList(videoId))
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
            throw new AnalyticsException("Failed to get video statistics", e);
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
