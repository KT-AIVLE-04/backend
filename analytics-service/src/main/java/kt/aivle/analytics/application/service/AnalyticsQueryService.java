package kt.aivle.analytics.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.out.AccountMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.PostCommentMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.PostMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.PostRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.analytics.domain.entity.AccountMetric;
import kt.aivle.analytics.domain.entity.Post;
import kt.aivle.analytics.domain.entity.PostCommentMetric;
import kt.aivle.analytics.domain.entity.PostMetric;
import kt.aivle.analytics.domain.entity.SnsAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsQueryService implements AnalyticsQueryUseCase {
    
    private final PostMetricRepositoryPort postMetricRepositoryPort;
    private final AccountMetricRepositoryPort accountMetricRepositoryPort;
    private final PostCommentMetricRepositoryPort postCommentMetricRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;
    
    @Override
    public List<PostMetricsQueryResponse> getPostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, dateRange: {}, accountId: {}, postId: {}", 
                userId, request.getDateRange(), request.getAccountId(), request.getPostId());
        
        LocalDateTime startDate = getStartDate(request.getDateRange());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<PostMetric> metrics;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            metrics = postMetricRepositoryPort.findByPostIdAndCrawledAtBetween(
                Long.parseLong(request.getPostId()), startDate, endDate);
        } else if (request.getAccountId() != null) {
            // 특정 계정의 모든 게시물 메트릭 조회
            metrics = postMetricRepositoryPort.findByAccountIdAndCrawledAtBetween(
                Long.parseLong(request.getAccountId()), startDate, endDate);
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> postMetricRepositoryPort.findByAccountIdAndCrawledAtBetween(accountId, startDate, endDate).stream())
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toPostMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AccountMetricsQueryResponse> getAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, dateRange: {}, accountId: {}", 
                userId, request.getDateRange(), request.getAccountId());
        
        LocalDateTime startDate = getStartDate(request.getDateRange());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<AccountMetric> metrics;
        if (request.getAccountId() != null) {
            // 특정 계정의 메트릭 조회
            metrics = accountMetricRepositoryPort.findBySnsAccountIdAndCrawledAtBetween(
                Long.parseLong(request.getAccountId()), startDate, endDate);
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> accountMetricRepositoryPort.findBySnsAccountIdAndCrawledAtBetween(accountId, startDate, endDate).stream())
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toAccountMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private LocalDateTime getStartDate(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        return switch (dateRange) {
            case "today" -> now.toLocalDate().atStartOfDay();
            case "3days" -> now.minusDays(3);
            case "1week" -> now.minusWeeks(1);
            case "1month" -> now.minusMonths(1);
            case "6months" -> now.minusMonths(6);
            case "1year" -> now.minusYears(1);
            default -> now.minusWeeks(1); // 기본값: 1주일
        };
    }
    
    private PostMetricsQueryResponse toPostMetricsQueryResponse(PostMetric metric) {
        // Post 엔티티에서 추가 정보 조회
        Post post = postRepositoryPort.findById(metric.getPostId()).orElse(null);
        
        return new PostMetricsQueryResponse(
            metric.getPostId(),
            post != null ? post.getSnsPostId() : "unknown",
            post != null ? post.getAccountId() : null,
            metric.getLikes(),
            metric.getDislikes(),
            metric.getComments(),
            metric.getShares(),
            metric.getViews(),
            metric.getCrawledAt()
        );
    }
    
    private AccountMetricsQueryResponse toAccountMetricsQueryResponse(AccountMetric metric) {
        // SnsAccount 엔티티에서 추가 정보 조회
        SnsAccount snsAccount = snsAccountRepositoryPort.findById(metric.getSnsAccountId()).orElse(null);
        
        return new AccountMetricsQueryResponse(
            snsAccount != null ? snsAccount.getId() : null,
            snsAccount != null ? snsAccount.getSnsAccountId() : "unknown",
            metric.getFollowers(),
            metric.getViews(),
            metric.getCrawledAt()
        );
    }
    
    @Override
    public List<PostCommentsQueryResponse> getPostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting post comments for userId: {}, dateRange: {}, postId: {}", 
                userId, request.getDateRange(), request.getPostId());
        
        LocalDateTime startDate = getStartDate(request.getDateRange());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<PostCommentMetric> comments;
        if (request.getPostId() != null) {
            // 특정 게시물의 댓글 조회
            comments = postCommentMetricRepositoryPort.findByPostIdAndCrawledAtBetween(
                Long.parseLong(request.getPostId()), startDate, endDate);
        } else {
            // 사용자의 모든 게시물 댓글 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            List<Long> postIds = postRepositoryPort.findByAccountId(accountIds.get(0)) // 첫 번째 계정의 게시물들
                .stream()
                .map(post -> post.getId())
                .collect(Collectors.toList());
            
            comments = postIds.stream()
                .flatMap(postId -> postCommentMetricRepositoryPort.findByPostIdAndCrawledAtBetween(postId, startDate, endDate).stream())
                .collect(Collectors.toList());
        }
        
        return comments.stream()
            .map(this::toPostCommentsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private PostCommentsQueryResponse toPostCommentsQueryResponse(PostCommentMetric comment) {
        // Post 엔티티에서 추가 정보 조회
        Post post = postRepositoryPort.findById(comment.getPostId()).orElse(null);
        
        return new PostCommentsQueryResponse(
            comment.getSnsCommentId(),
            post != null ? post.getSnsPostId() : "unknown",
            post != null ? post.getAccountId() : null,
            comment.getContent(),
            comment.getCrawledAt()
        );
    }
}
