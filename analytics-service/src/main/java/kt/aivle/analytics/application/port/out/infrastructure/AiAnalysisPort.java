package kt.aivle.analytics.application.port.out.infrastructure;

import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.domain.model.SentimentType;

/**
 * AI 분석을 위한 Port 인터페이스
 * 외부 AI 서버와의 통신을 추상화
 */
public interface AiAnalysisPort {
    
    /**
     * AI 감정분석 요청
     */
    AiAnalysisResponse analyzeComments(List<PostCommentsQueryResponse> comments, Long postId);
    
    /**
     * AI 분석 응답 DTO
     */
    class AiAnalysisResponse {
        private final EmotionAnalysis emotionAnalysis;
        private final Keywords keywords;
        
        public AiAnalysisResponse(EmotionAnalysis emotionAnalysis, Keywords keywords) {
            this.emotionAnalysis = emotionAnalysis;
            this.keywords = keywords;
        }
        
        public EmotionAnalysis getEmotionAnalysis() { return emotionAnalysis; }
        public Keywords getKeywords() { return keywords; }
        
        public static class EmotionAnalysis {
            private final List<IndividualResult> individualResults;
            
            public EmotionAnalysis(List<IndividualResult> individualResults) {
                this.individualResults = individualResults;
            }
            
            public List<IndividualResult> getIndividualResults() { return individualResults; }
        }
        
        public static class IndividualResult {
            private final String id;
            private final SentimentType result;
            
            public IndividualResult(String id, SentimentType result) {
                this.id = id;
                this.result = result;
            }
            
            public String getId() { return id; }
            public SentimentType getResult() { return result; }
        }
        
        public static class Keywords {
            private final List<String> positive;
            private final List<String> negative;
            
            public Keywords(List<String> positive, List<String> negative) {
                this.positive = positive;
                this.negative = negative;
            }
            
            public List<String> getPositive() { return positive; }
            public List<String> getNegative() { return negative; }
        }
    }
}
