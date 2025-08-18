package kt.aivle.analytics.application.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 캐시 TTL 설정
    private static final Duration METRICS_CACHE_DURATION = Duration.ofMinutes(5); // 메트릭 5분
    private static final Duration COMMENTS_CACHE_DURATION = Duration.ofMinutes(2); // 댓글 2분 (더 자주 변경)
    private static final Duration QUOTA_CACHE_DURATION = Duration.ofMinutes(1); // 할당량 1분
    
    private static final String CACHE_PREFIX = "analytics:";
    
    /**
     * 실시간 게시물 메트릭을 캐시에 저장합니다.
     */
    public void cacheRealtimePostMetrics(Long postId, List<RealtimePostMetricsResponse> metrics) {
        try {
            String key = generatePostMetricsKey(postId);
            String value = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(key, value, METRICS_CACHE_DURATION);
            log.debug("Cached realtime post metrics for postId: {}", postId);
        } catch (Exception e) {
            log.warn("Failed to cache realtime post metrics for postId: {}", postId, e);
        }
    }
    
    /**
     * 캐시에서 실시간 게시물 메트릭을 조회합니다.
     */
    public Optional<List<RealtimePostMetricsResponse>> getCachedRealtimePostMetrics(Long postId) {
        try {
            String key = generatePostMetricsKey(postId);
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                List<RealtimePostMetricsResponse> metrics = objectMapper.readValue(
                    cached.toString(), 
                    new TypeReference<List<RealtimePostMetricsResponse>>() {}
                );
                log.debug("Retrieved cached realtime post metrics for postId: {}", postId);
                return Optional.of(metrics);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached realtime post metrics for postId: {}", postId, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 실시간 계정 메트릭을 캐시에 저장합니다.
     */
    public void cacheRealtimeAccountMetrics(Long accountId, List<RealtimeAccountMetricsResponse> metrics) {
        try {
            String key = generateAccountMetricsKey(accountId);
            String value = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(key, value, METRICS_CACHE_DURATION);
            log.debug("Cached realtime account metrics for accountId: {}", accountId);
        } catch (Exception e) {
            log.warn("Failed to cache realtime account metrics for accountId: {}", accountId, e);
        }
    }
    
    /**
     * 캐시에서 실시간 계정 메트릭을 조회합니다.
     */
    public Optional<List<RealtimeAccountMetricsResponse>> getCachedRealtimeAccountMetrics(Long accountId) {
        try {
            String key = generateAccountMetricsKey(accountId);
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                List<RealtimeAccountMetricsResponse> metrics = objectMapper.readValue(
                    cached.toString(), 
                    new TypeReference<List<RealtimeAccountMetricsResponse>>() {}
                );
                log.debug("Retrieved cached realtime account metrics for accountId: {}", accountId);
                return Optional.of(metrics);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached realtime account metrics for accountId: {}", accountId, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 실시간 게시물 댓글을 캐시에 저장합니다.
     */
    public void cacheRealtimePostComments(Long postId, Integer page, Integer size, List<PostCommentsQueryResponse> comments) {
        try {
            String key = generatePostCommentsKey(postId, page, size);
            String value = objectMapper.writeValueAsString(comments);
            redisTemplate.opsForValue().set(key, value, COMMENTS_CACHE_DURATION);
            log.debug("Cached realtime post comments for postId: {}, page: {}, size: {}", postId, page, size);
        } catch (Exception e) {
            log.warn("Failed to cache realtime post comments for postId: {}", postId, e);
        }
    }
    
    /**
     * 캐시에서 실시간 게시물 댓글을 조회합니다.
     */
    public Optional<List<PostCommentsQueryResponse>> getCachedRealtimePostComments(Long postId, Integer page, Integer size) {
        try {
            String key = generatePostCommentsKey(postId, page, size);
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                List<PostCommentsQueryResponse> comments = objectMapper.readValue(
                    cached.toString(), 
                    new TypeReference<List<PostCommentsQueryResponse>>() {}
                );
                log.debug("Retrieved cached realtime post comments for postId: {}, page: {}, size: {}", postId, page, size);
                return Optional.of(comments);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached realtime post comments for postId: {}", postId, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 할당량 정보를 캐시에 저장합니다.
     */
    public void cacheQuotaStatus(String key, Object quotaStatus) {
        try {
            String value = objectMapper.writeValueAsString(quotaStatus);
            redisTemplate.opsForValue().set(key, value, QUOTA_CACHE_DURATION);
            log.debug("Cached quota status for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to cache quota status for key: {}", key, e);
        }
    }
    
    /**
     * 캐시에서 할당량 정보를 조회합니다.
     */
    public <T> Optional<T> getCachedQuotaStatus(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                T quotaStatus = objectMapper.readValue(cached.toString(), type);
                log.debug("Retrieved cached quota status for key: {}", key);
                return Optional.of(quotaStatus);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached quota status for key: {}", key, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 특정 패턴의 캐시를 삭제합니다.
     */
    public void evictCache(String pattern) {
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.debug("Evicted cache for pattern: {}", pattern);
        } catch (Exception e) {
            log.warn("Failed to evict cache for pattern: {}", pattern, e);
        }
    }
    
    /**
     * 특정 키의 캐시를 삭제합니다.
     */
    public void evictCacheByKey(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to evict cache for key: {}", key, e);
        }
    }
    
    /**
     * 게시물 관련 모든 캐시를 삭제합니다.
     */
    public void evictPostCache(Long postId) {
        String pattern = CACHE_PREFIX + "post:*:" + postId + "*";
        evictCache(pattern);
    }
    
    /**
     * 계정 관련 모든 캐시를 삭제합니다.
     */
    public void evictAccountCache(Long accountId) {
        String pattern = CACHE_PREFIX + "account:*:" + accountId + "*";
        evictCache(pattern);
    }
    
    // 캐시 키 생성 메서드들
    private String generatePostMetricsKey(Long postId) {
        return CACHE_PREFIX + "post:metrics:" + postId;
    }
    
    private String generateAccountMetricsKey(Long accountId) {
        return CACHE_PREFIX + "account:metrics:" + accountId;
    }
    
    private String generatePostCommentsKey(Long postId, Integer page, Integer size) {
        return CACHE_PREFIX + "post:comments:" + postId + ":" + page + ":" + size;
    }
    
    public String generateQuotaStatusKey() {
        return CACHE_PREFIX + "quota:status";
    }
}
