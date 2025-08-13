package kt.aivle.analytics.adapter.out.external;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import kt.aivle.analytics.application.port.out.ExternalApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApiAdapter implements ExternalApiPort {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-server.url:http://localhost:8085}")
    private String aiServerUrl;
    
    @Value("${app.youtube.api.key}")
    private String youtubeApiKey;
    
    @Override
    public List<VideoData> getYouTubeVideos(String accessToken, String channelId) {
        try {
            // 1. 먼저 채널 정보 조회 (mine=true 사용)
            String channelUrl = "https://www.googleapis.com/youtube/v3/channels?part=snippet&mine=true";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> channelResponse = restTemplate.exchange(
                channelUrl, HttpMethod.GET, entity, JsonNode.class);
            
            JsonNode channelData = channelResponse.getBody();
            if (channelData == null || channelData.get("items") == null || channelData.get("items").isEmpty()) {
                log.warn("채널 정보를 찾을 수 없음");
                return List.of();
            }
            
            String actualChannelId = channelData.get("items").get(0).get("id").asText();
            
            // 2. 해당 채널의 비디오 목록 조회
            String searchUrl = String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=%s&maxResults=50&order=date&type=video", 
                actualChannelId);
            
            ResponseEntity<JsonNode> searchResponse = restTemplate.exchange(
                searchUrl, HttpMethod.GET, entity, JsonNode.class);
            
            JsonNode searchData = searchResponse.getBody();
            if (searchData == null || searchData.get("items") == null) {
                return List.of();
            }

            // search.list 응답은 videoId가 items[].id.videoId 에 있음 (snippet.resourceId 아님)
            java.util.List<VideoData> videos = new java.util.ArrayList<>();
            for (JsonNode item : searchData.get("items")) {
                JsonNode idNode = item.get("id");
                JsonNode snippet = item.get("snippet");
                if (idNode == null || snippet == null || idNode.get("videoId") == null) {
                    continue;
                }
                videos.add(new VideoData(
                    idNode.get("videoId").asText(),
                    snippet.get("title").asText(),
                    snippet.get("description").asText(),
                    snippet.get("publishedAt").asText()
                ));
            }
            return videos;
                
        } catch (Exception e) {
            log.error("YouTube 비디오 목록 조회 실패", e);
            return List.of();
        }
    }
    
    @Override
    public VideoMetrics getYouTubeVideoMetrics(String accessToken, String videoId) {
        try {
            // YouTube Analytics API 사용하여 더 상세한 메트릭 조회
            String url = "https://youtubeanalytics.googleapis.com/v2/reports";
            String params = String.format(
                "ids=channel==MINE&startDate=%s&endDate=%s&metrics=views,likes,shares,comments&dimensions=video&filters=video==%s",
                getDateString(-7), // 7일 전부터
                getDateString(0),  // 오늘까지
                videoId
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url + "?" + params, HttpMethod.GET, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null || data.get("rows") == null || data.get("rows").isEmpty()) {
                log.warn("YouTube Analytics API에서 비디오 메트릭을 찾을 수 없음: videoId={}", videoId);
                // fallback: YouTube Data API v3 사용
                return getYouTubeVideoMetricsFallback(accessToken, videoId);
            }
            
            JsonNode row = data.get("rows").get(0);
            // rows: [videoId, views, likes, shares, comments]
            return new VideoMetrics(
                row.get(1).asLong(0),
                row.get(2).asLong(0),
                row.get(4).asLong(0), // comments
                row.get(3).asLong(0)  // shares
            );
            
        } catch (Exception e) {
            log.error("YouTube Analytics API 호출 실패: videoId={}", videoId, e);
            // fallback: YouTube Data API v3 사용
            return getYouTubeVideoMetricsFallback(accessToken, videoId);
        }
    }
    
    private VideoMetrics getYouTubeVideoMetricsFallback(String accessToken, String videoId) {
        try {
            String url = String.format("https://www.googleapis.com/youtube/v3/videos?part=statistics&id=%s", videoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null || data.get("items") == null || data.get("items").isEmpty()) {
                log.warn("비디오 정보를 찾을 수 없음: videoId={}", videoId);
                return new VideoMetrics(0L, 0L, 0L, 0L);
            }
            
            JsonNode statistics = data.get("items").get(0).get("statistics");
            
            return new VideoMetrics(
                Long.parseLong(statistics.get("viewCount").asText("0")),
                Long.parseLong(statistics.get("likeCount").asText("0")),
                Long.parseLong(statistics.get("commentCount").asText("0")),
                0L // shareCount는 YouTube Analytics API에서만 제공
            );
            
        } catch (Exception e) {
            log.error("YouTube Data API v3 fallback 실패: videoId={}", videoId, e);
            return new VideoMetrics(0L, 0L, 0L, 0L);
        }
    }
    
    private String getDateString(int daysOffset) {
        return java.time.LocalDate.now().plusDays(daysOffset).toString();
    }
    
    @Override
    public List<ExternalApiPort.CommentData> getYouTubeComments(String accessToken, String videoId) {
        try {
            String url = String.format("https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId=%s&maxResults=100", videoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null || data.get("items") == null) {
                return List.of();
            }
            
            java.util.List<ExternalApiPort.CommentData> comments = new java.util.ArrayList<>();
            for (JsonNode item : data.get("items")) {
                JsonNode snippet = item.get("snippet");
                if (snippet == null) continue;
                JsonNode topLevelComment = snippet.get("topLevelComment");
                if (topLevelComment == null) continue;
                String commentId = topLevelComment.get("id") != null ? topLevelComment.get("id").asText() : "";
                JsonNode commentSnippet = topLevelComment.get("snippet");
                if (commentSnippet == null) continue;
                comments.add(new ExternalApiPort.CommentData(
                    commentId,
                    commentSnippet.get("textDisplay").asText(),
                    commentSnippet.get("authorDisplayName").asText(),
                    commentSnippet.get("publishedAt").asText()
                ));
            }
            return comments;
                
        } catch (Exception e) {
            log.error("YouTube 댓글 조회 실패: videoId={}", videoId, e);
            return List.of();
        }
    }
    
    @Override
    public ChannelData getYouTubeChannelData(String accessToken, String channelId) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/channels?part=statistics,snippet&mine=true";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null || data.get("items") == null || data.get("items").isEmpty()) {
                log.warn("채널 정보를 찾을 수 없음");
                return new ChannelData("unknown", "Unknown Channel", 0L, 0L);
            }
            
            JsonNode channel = data.get("items").get(0);
            JsonNode statistics = channel.get("statistics");
            JsonNode snippet = channel.get("snippet");
            
            return new ChannelData(
                channel.get("id").asText(),
                snippet.get("title").asText(),
                Long.parseLong(statistics.get("subscriberCount").asText("0")),
                Long.parseLong(statistics.get("videoCount").asText("0"))
            );
            
        } catch (Exception e) {
            log.error("YouTube 채널 데이터 조회 실패", e);
            return new ChannelData("unknown", "Unknown Channel", 0L, 0L);
        }
    }
    
    @Override
    public SentimentAnalysisResult analyzeSentiment(String videoId, List<String> comments) {
        try {
            Map<String, Object> request = Map.of(
                "videoId", videoId,
                "comments", comments
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                aiServerUrl + "/api/ai/sentiment", HttpMethod.POST, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null) {
                return new SentimentAnalysisResult(0.0, 0.0, 1.0, "분석 실패");
            }
            
            return new SentimentAnalysisResult(
                data.get("positiveScore").asDouble(),
                data.get("negativeScore").asDouble(),
                data.get("neutralScore").asDouble(),
                data.get("summary").asText()
            );
            
        } catch (Exception e) {
            log.error("감정 분석 실패: videoId={}", videoId, e);
            return new SentimentAnalysisResult(0.0, 0.0, 1.0, "분석 실패");
        }
    }
    
    @Override
    public TrendAnalysisResult analyzeTrends(String userId, Map<String, Object> metrics) {
        try {
            Map<String, Object> request = Map.of(
                "userId", userId,
                "metrics", metrics
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                aiServerUrl + "/api/ai/trends", HttpMethod.POST, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null) {
                return new TrendAnalysisResult("안정적", 0.5, "추가 데이터 필요");
            }
            
            return new TrendAnalysisResult(
                data.get("trend").asText(),
                data.get("confidence").asDouble(),
                data.get("recommendation").asText()
            );
            
        } catch (Exception e) {
            log.error("트렌드 분석 실패: userId={}", userId, e);
            return new TrendAnalysisResult("안정적", 0.5, "추가 데이터 필요");
        }
    }
    
    @Override
    public OptimalTimeResult analyzeOptimalPostingTime(String userId, Map<String, Object> postingData) {
        try {
            Map<String, Object> request = Map.of(
                "userId", userId,
                "postingData", postingData
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                aiServerUrl + "/api/ai/optimal-time", HttpMethod.POST, entity, JsonNode.class);
            
            JsonNode data = response.getBody();
            if (data == null) {
                return new OptimalTimeResult("월요일", "18:00", 5.0);
            }
            
            return new OptimalTimeResult(
                data.get("optimalDay").asText(),
                data.get("optimalTime").asText(),
                data.get("expectedEngagement").asDouble()
            );
            
        } catch (Exception e) {
            log.error("최적 게시 시간 분석 실패: userId={}", userId, e);
            return new OptimalTimeResult("월요일", "18:00", 5.0);
        }
    }
    
    @Override
    public TokenRefreshResult refreshToken(String refreshToken, String snsType) {
        try {
            if ("youtube".equalsIgnoreCase(snsType)) {
                return refreshYouTubeToken(refreshToken);
            }
            throw new UnsupportedOperationException("지원하지 않는 SNS 타입: " + snsType);
            
        } catch (Exception e) {
            log.error("토큰 갱신 실패: snsType={}", snsType, e);
            throw new RuntimeException("토큰 갱신 실패", e);
        }
    }
    
    private TokenRefreshResult refreshYouTubeToken(String refreshToken) {
        // Google OAuth2 토큰 갱신 로직
        // 실제 구현에서는 Google OAuth2 API를 호출해야 함
        return new TokenRefreshResult("new_access_token", refreshToken, System.currentTimeMillis() + 3600000);
    }
}
