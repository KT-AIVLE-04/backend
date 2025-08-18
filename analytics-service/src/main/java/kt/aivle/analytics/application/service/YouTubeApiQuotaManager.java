package kt.aivle.analytics.application.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class YouTubeApiQuotaManager {
    
    @Value("${app.youtube.api.quota-limit:10000}")
    private int quotaLimit;
    
    @Value("${app.youtube.api.quota-window:86400}")
    private long quotaWindowSeconds;
    
    private final AtomicInteger apiCallCount = new AtomicInteger(0);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());
    
    // 캐시 설정
    private final Cache<String, Object> metricsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)  // 5분 캐시
        .maximumSize(1000)
        .build();
    
    private final Cache<String, Object> commentsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)  // 댓글은 2분 캐시 (더 자주 변경됨)
        .maximumSize(500)
        .build();
    
    /**
     * API 할당량 체크
     * @param priority API 우선순위
     * @return 할당량 사용 가능 여부
     */
    public boolean checkQuotaLimit(ApiPriority priority) {
        long now = System.currentTimeMillis();
        long lastReset = lastResetTime.get();
        
        // 할당량 윈도우 경과 시 카운트 리셋
        if (now - lastReset > quotaWindowSeconds * 1000) {
            apiCallCount.set(0);
            lastResetTime.set(now);
            log.info("YouTube API quota reset. New day started.");
        }
        
        int currentUsage = apiCallCount.get();
        int limit = switch (priority) {
            case BATCH_COLLECTION -> (int) (quotaLimit * 0.8);  // 배치 작업용 80%
            case REAL_TIME_QUERY -> (int) (quotaLimit * 0.95);  // 실시간 조회용 95%
            case BACKGROUND_SYNC -> (int) (quotaLimit * 0.98);  // 백그라운드용 98%
        };
        
        boolean available = currentUsage < limit;
        
        if (!available) {
            log.warn("YouTube API quota limit reached. Current: {}, Limit: {}, Priority: {}", 
                currentUsage, limit, priority);
        }
        
        return available;
    }
    
    /**
     * API 호출 카운트 증가
     */
    public void incrementApiCall() {
        int current = apiCallCount.incrementAndGet();
        log.debug("YouTube API call count: {}", current);
    }
    
    /**
     * 현재 할당량 사용량 조회
     */
    public QuotaStatus getQuotaStatus() {
        long now = System.currentTimeMillis();
        long lastReset = lastResetTime.get();
        long timeUntilReset = (quotaWindowSeconds * 1000) - (now - lastReset);
        
        return new QuotaStatus(
            apiCallCount.get(),
            quotaLimit,
            timeUntilReset,
            lastReset
        );
    }
    
    /**
     * 메트릭 캐시에서 데이터 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromMetricsCache(String key, Class<T> type) {
        return (T) metricsCache.getIfPresent(key);
    }
    
    /**
     * 메트릭 캐시에 데이터 저장
     */
    public void putToMetricsCache(String key, Object value) {
        metricsCache.put(key, value);
        log.debug("Cached metrics data for key: {}", key);
    }
    
    /**
     * 캐시 키 생성
     */
    public String generateMetricsCacheKey(String type, String id, String dateRange) {
        return String.format("metrics:%s:%s:%s", type, id, dateRange);
    }
    
    public String generateCommentsCacheKey(String postId, String dateRange, int page, int size) {
        return String.format("comments:%s:%s:%d:%d", postId, dateRange, page, size);
    }
    
    /**
     * 댓글 캐시에서 데이터 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromCommentsCache(String key, Class<T> type) {
        return (T) commentsCache.getIfPresent(key);
    }
    
    /**
     * 댓글 캐시에 데이터 저장
     */
    public void putToCommentsCache(String key, Object value) {
        commentsCache.put(key, value);
        log.debug("Cached comments data for key: {}", key);
    }
    
    /**
     * 캐시 무효화
     */
    public void invalidateCache(String key) {
        metricsCache.invalidate(key);
        commentsCache.invalidate(key);
        log.debug("Invalidated cache for key: {}", key);
    }
    
    /**
     * API 우선순위
     */
    public enum ApiPriority {
        BATCH_COLLECTION,    // 배치 작업 (높은 우선순위)
        REAL_TIME_QUERY,     // 실시간 조회 (중간 우선순위)
        BACKGROUND_SYNC      // 백그라운드 동기화 (낮은 우선순위)
    }
    
    /**
     * 할당량 상태 정보
     */
    public static class QuotaStatus {
        private final int currentUsage;
        private final int totalLimit;
        private final long timeUntilReset;
        private final long lastResetTime;
        
        public QuotaStatus(int currentUsage, int totalLimit, long timeUntilReset, long lastResetTime) {
            this.currentUsage = currentUsage;
            this.totalLimit = totalLimit;
            this.timeUntilReset = timeUntilReset;
            this.lastResetTime = lastResetTime;
        }
        
        public int getCurrentUsage() { return currentUsage; }
        public int getTotalLimit() { return totalLimit; }
        public long getTimeUntilReset() { return timeUntilReset; }
        public long getLastResetTime() { return lastResetTime; }
        public double getUsagePercentage() { return (double) currentUsage / totalLimit * 100; }
    }
}
