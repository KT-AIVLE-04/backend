package kt.aivle.analytics.adapter.out.infrastructure;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.application.port.out.infrastructure.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 캐시 TTL 설정
    private static final Duration METRICS_CACHE_DURATION = Duration.ofMinutes(5);
    private static final Duration COMMENTS_CACHE_DURATION = Duration.ofMinutes(2);
    private static final Duration QUOTA_CACHE_DURATION = Duration.ofMinutes(1);
    
    private static final String CACHE_PREFIX = "analytics:";
    
    @Override
    public void cacheRealtimePostMetrics(Long postId, List<PostMetricsResponse> metrics) {
        try {
            String key = generatePostMetricsKey(postId);
            String value = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(key, value, METRICS_CACHE_DURATION);
            log.debug("Cached realtime post metrics for postId: {}", postId);
        } catch (Exception e) {
            log.warn("Failed to cache realtime post metrics for postId: {}", postId, e);
        }
    }
    
    @Override
    public Optional<List<PostMetricsResponse>> getCachedRealtimePostMetrics(Long postId) {
        try {
            String key = generatePostMetricsKey(postId);
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                List<PostMetricsResponse> metrics = objectMapper.readValue(
                    cached.toString(), 
                    new TypeReference<List<PostMetricsResponse>>() {}
                );
                log.debug("Retrieved cached realtime post metrics for postId: {}", postId);
                return Optional.of(metrics);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached realtime post metrics for postId: {}", postId, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void cacheRealtimeAccountMetrics(Long accountId, List<AccountMetricsResponse> metrics) {
        try {
            String key = generateAccountMetricsKey(accountId);
            String value = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(key, value, METRICS_CACHE_DURATION);
            log.debug("Cached realtime account metrics for accountId: {}", accountId);
        } catch (Exception e) {
            log.warn("Failed to cache realtime account metrics for accountId: {}", accountId, e);
        }
    }
    
    @Override
    public Optional<List<AccountMetricsResponse>> getCachedRealtimeAccountMetrics(Long accountId) {
        try {
            String key = generateAccountMetricsKey(accountId);
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                List<AccountMetricsResponse> metrics = objectMapper.readValue(
                    cached.toString(), 
                    new TypeReference<List<AccountMetricsResponse>>() {}
                );
                log.debug("Retrieved cached realtime account metrics for accountId: {}", accountId);
                return Optional.of(metrics);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached realtime account metrics for accountId: {}", accountId, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void cachePostComments(Long postId, List<PostCommentsResponse> comments) {
        try {
            String key = generateCommentsKey(postId);
            String value = objectMapper.writeValueAsString(comments);
            redisTemplate.opsForValue().set(key, value, COMMENTS_CACHE_DURATION);
            log.debug("Cached post comments for postId: {}", postId);
        } catch (Exception e) {
            log.warn("Failed to cache post comments for postId: {}", postId, e);
        }
    }
    
    @Override
    public Optional<List<PostCommentsResponse>> getCachedPostComments(Long postId) {
        try {
            String key = generateCommentsKey(postId);
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                List<PostCommentsResponse> comments = objectMapper.readValue(
                    cached.toString(), 
                    new TypeReference<List<PostCommentsResponse>>() {}
                );
                log.debug("Retrieved cached post comments for postId: {}", postId);
                return Optional.of(comments);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached post comments for postId: {}", postId, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void cacheQuotaInfo(String key, Object quotaInfo) {
        try {
            String cacheKey = generateQuotaKey(key);
            String value = objectMapper.writeValueAsString(quotaInfo);
            redisTemplate.opsForValue().set(cacheKey, value, QUOTA_CACHE_DURATION);
            log.debug("Cached quota info for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to cache quota info for key: {}", key, e);
        }
    }
    
    @Override
    public Optional<Object> getCachedQuotaInfo(String key) {
        try {
            String cacheKey = generateQuotaKey(key);
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                log.debug("Retrieved cached quota info for key: {}", key);
                return Optional.of(cached);
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached quota info for key: {}", key, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void evictPostCache(Long postId) {
        try {
            String metricsKey = generatePostMetricsKey(postId);
            String commentsKey = generateCommentsKey(postId);
            
            redisTemplate.delete(metricsKey);
            redisTemplate.delete(commentsKey);
            
            log.debug("Evicted post cache for postId: {}", postId);
        } catch (Exception e) {
            log.warn("Failed to evict post cache for postId: {}", postId, e);
        }
    }
    
    @Override
    public void evictAccountCache(Long accountId) {
        try {
            String metricsKey = generateAccountMetricsKey(accountId);
            redisTemplate.delete(metricsKey);
            
            log.debug("Evicted account cache for accountId: {}", accountId);
        } catch (Exception e) {
            log.warn("Failed to evict account cache for accountId: {}", accountId, e);
        }
    }
    
    @Override
    public void evictCommentsCache(Long postId) {
        try {
            String commentsKey = generateCommentsKey(postId);
            redisTemplate.delete(commentsKey);
            
            log.debug("Evicted comments cache for postId: {}", postId);
        } catch (Exception e) {
            log.warn("Failed to evict comments cache for postId: {}", postId, e);
        }
    }
    
    @Override
    public void evictCache(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to evict cache for key: {}", key, e);
        }
    }
    
    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check cache existence for key: {}", key, e);
            return false;
        }
    }
    
    // 캐시 키 생성 메서드들
    private String generatePostMetricsKey(Long postId) {
        return CACHE_PREFIX + "post:metrics:" + postId;
    }
    
    private String generateAccountMetricsKey(Long accountId) {
        return CACHE_PREFIX + "account:metrics:" + accountId;
    }
    
    private String generateCommentsKey(Long postId) {
        return CACHE_PREFIX + "comments:" + postId;
    }
    
    private String generateQuotaKey(String key) {
        return CACHE_PREFIX + "quota:" + key;
    }
}
