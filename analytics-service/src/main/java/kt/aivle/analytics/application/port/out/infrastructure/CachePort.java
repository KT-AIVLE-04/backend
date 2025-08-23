package kt.aivle.analytics.application.port.out.infrastructure;

import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;

/**
 * 캐시 관리를 위한 Port 인터페이스
 * Redis 캐시 구현을 추상화하여 애플리케이션 계층과 분리
 */
public interface CachePort {
    
    /**
     * 실시간 게시물 메트릭 캐시 저장
     */
    void cacheRealtimePostMetrics(Long postId, List<PostMetricsResponse> metrics);
    
    /**
     * 캐시에서 실시간 게시물 메트릭 조회
     */
    Optional<List<PostMetricsResponse>> getCachedRealtimePostMetrics(Long postId);
    
    /**
     * 실시간 계정 메트릭 캐시 저장
     */
    void cacheRealtimeAccountMetrics(Long accountId, List<AccountMetricsResponse> metrics);
    
    /**
     * 캐시에서 실시간 계정 메트릭 조회
     */
    Optional<List<AccountMetricsResponse>> getCachedRealtimeAccountMetrics(Long accountId);
    
    /**
     * 댓글 데이터 캐시 저장
     */
    void cachePostComments(Long postId, List<PostCommentsResponse> comments);
    
    /**
     * 캐시에서 댓글 데이터 조회
     */
    Optional<List<PostCommentsResponse>> getCachedPostComments(Long postId);
    
    /**
     * API 할당량 정보 캐시 저장
     */
    void cacheQuotaInfo(String key, Object quotaInfo);
    
    /**
     * 캐시에서 API 할당량 정보 조회
     */
    Optional<Object> getCachedQuotaInfo(String key);
    
    /**
     * 게시물 관련 캐시 무효화
     */
    void evictPostCache(Long postId);
    
    /**
     * 계정 관련 캐시 무효화
     */
    void evictAccountCache(Long accountId);
    
    /**
     * 댓글 캐시 무효화
     */
    void evictCommentsCache(Long postId);
    
    /**
     * 특정 키의 캐시 무효화
     */
    void evictCache(String key);
    
    /**
     * 캐시 존재 여부 확인
     */
    boolean exists(String key);
}
