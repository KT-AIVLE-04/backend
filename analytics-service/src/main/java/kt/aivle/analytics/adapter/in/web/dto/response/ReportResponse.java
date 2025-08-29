package kt.aivle.analytics.adapter.in.web.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long postId;
    private String markdownReport;
    
    // AI 서버에 보낼 데이터
    private MetricsData metrics;
    private EmotionData emotionData;
    private String title;
    private String description;
    private String url;
    private List<String> tags;
    private String publishAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsData {
        private Long postId;
        private Long viewCount;
        private Long likeCount;
        private Long commentCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionData {
        private Long positiveCount;
        private Long negativeCount;
        private Long neutralCount;
        private List<String> positiveKeywords;
        private List<String> negativeKeywords;
        private List<String> neutralKeywords;
    }
}
