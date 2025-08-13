package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import kt.aivle.analytics.domain.entity.AnalysisResult;

public record AnalysisResultResponse(
    String userId,
    String snsType,
    String analysisType,
    LocalDateTime periodStart,
    LocalDateTime periodEnd,
    Double score,
    String summary,
    String recommendations
) {
    public static AnalysisResultResponse from(AnalysisResult result) {
        return new AnalysisResultResponse(
            result.getUserId(),
            result.getSnsType().name(),
            result.getAnalysisType().name(),
            result.getPeriodStart(),
            result.getPeriodEnd(),
            result.getScore(),
            result.getSummary(),
            result.getRecommendations()
        );
    }
}
