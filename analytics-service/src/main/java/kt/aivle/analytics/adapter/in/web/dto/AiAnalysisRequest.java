package kt.aivle.analytics.adapter.in.web.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisRequest {
    private List<CommentData> data;
    private Keywords keyword;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentData {
        private String id;
        private String result;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keywords {
        private List<String> positive;
        private List<String> negative;
    }
}
