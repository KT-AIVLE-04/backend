package kt.aivle.analytics.application.port.out.infrastructure;

import java.util.List;

import kt.aivle.analytics.application.port.out.dto.AiAnalysisResponse;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;

/**
 * AI 분석을 위한 Port 인터페이스
 * 외부 AI 서버와의 통신을 추상화
 */
public interface AiAnalysisPort {
    
    /**
     * AI 감정분석 요청
     */
    AiAnalysisResponse analyzeComments(List<SnsPostCommentMetric> comments, Long postId);
    

}
