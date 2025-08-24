package kt.aivle.analytics.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/analytics/history")
@RequiredArgsConstructor
@Tag(name = "Historical Analytics", description = "히스토리 분석 API")
public class HistoricalAnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    
    @Operation(summary = "히스토리 게시물 메트릭 조회", description = "특정 날짜의 게시물 메트릭 히스토리를 조회합니다.")
    @GetMapping("/posts/metrics")
    public ResponseEntity<ApiResponse<PostMetricsResponse>> getHistoricalPostMetrics(
            @RequestParam("date") String dateStr,
            @RequestParam("snsType") String snsType,
            @RequestParam(value = "postId", required = false) String postId,
            @RequestHeader("X-USER-ID") String userId) {
        
        PostMetricsResponse response = analyticsQueryUseCase.getHistoricalPostMetrics(userId, dateStr, snsType, postId);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 계정 메트릭 조회", description = "특정 날짜의 계정 메트릭 히스토리를 조회합니다.")
    @GetMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<AccountMetricsResponse>> getHistoricalAccountMetrics(
            @RequestParam("date") String dateStr,
            @RequestParam("snsType") String snsType,
            @RequestHeader("X-USER-ID") String userId) {
        
        AccountMetricsResponse response = analyticsQueryUseCase.getHistoricalAccountMetrics(userId, dateStr, snsType);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 게시물 댓글 조회", description = "게시물 댓글 히스토리를 페이지네이션으로 조회합니다.")
    @GetMapping("/posts/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsResponse>>> getHistoricalPostComments(
            @RequestParam("date") String dateStr,
            @RequestParam("snsType") String snsType,
            @RequestParam(value = "postId", required = false) String postId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestHeader("X-USER-ID") String userId) {
        
        List<PostCommentsResponse> response = analyticsQueryUseCase.getHistoricalPostComments(userId, dateStr, snsType, postId, page, size);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 게시물 감정분석 조회", description = "특정 날짜의 게시물 댓글 감정분석 결과와 키워드를 조회합니다.")
    @GetMapping("/posts/emotion-analysis")
    public ResponseEntity<ApiResponse<EmotionAnalysisResponse>> getHistoricalEmotionAnalysis(
            @RequestParam("date") String dateStr,
            @RequestParam("snsType") String snsType,
            @RequestParam("postId") String postId,
            @RequestHeader("X-USER-ID") String userId) {
        
        EmotionAnalysisResponse response = analyticsQueryUseCase.getHistoricalEmotionAnalysis(userId, dateStr, snsType, postId);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
}
