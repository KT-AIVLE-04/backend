package kt.aivle.analytics.application.port.in;

import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;

public interface AnalyticsQueryUseCase {
    
    List<PostMetricsQueryResponse> getPostMetrics(String userId, PostMetricsQueryRequest request);
    
    List<AccountMetricsQueryResponse> getAccountMetrics(String userId, AccountMetricsQueryRequest request);
    
    List<PostCommentsQueryResponse> getPostComments(String userId, PostCommentsQueryRequest request);
}
