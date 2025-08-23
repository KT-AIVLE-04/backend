package kt.aivle.analytics.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CacheConfig {
    
    // 히스토리 데이터 (DB 조회) - 상대적으로 긴 TTL
    @Value("${app.cache.ttl.post-metrics:300}")
    private long postMetricsTtl;
    
    @Value("${app.cache.ttl.account-metrics:300}")
    private long accountMetricsTtl;
    
    @Value("${app.cache.ttl.emotion-analysis:600}")
    private long emotionAnalysisTtl;
    
    // 실시간 데이터 (외부 API 호출) - 짧은 TTL
    @Value("${app.cache.ttl.realtime-post-metrics:60}")
    private long realtimePostMetricsTtl;
    
    @Value("${app.cache.ttl.realtime-account-metrics:60}")
    private long realtimeAccountMetricsTtl;
    
    @Value("${app.cache.ttl.comments:120}")
    private long commentsTtl;
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("캐시 설정 - 히스토리: post-metrics: {}초, account-metrics: {}초, emotion-analysis: {}초", 
                postMetricsTtl, accountMetricsTtl, emotionAnalysisTtl);
        log.info("캐시 설정 - 실시간: realtime-post-metrics: {}초, realtime-account-metrics: {}초, comments: {}초", 
                realtimePostMetricsTtl, realtimeAccountMetricsTtl, commentsTtl);
        
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(300)) // 기본 5분
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 캐시별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 히스토리 데이터 (DB 조회) - 상대적으로 긴 TTL
        cacheConfigurations.put("post-metrics", defaultConfig.entryTtl(Duration.ofSeconds(postMetricsTtl)));
        cacheConfigurations.put("account-metrics", defaultConfig.entryTtl(Duration.ofSeconds(accountMetricsTtl)));
        cacheConfigurations.put("emotion-analysis", defaultConfig.entryTtl(Duration.ofSeconds(emotionAnalysisTtl)));
        
        // 실시간 데이터 (외부 API 호출) - 짧은 TTL
        cacheConfigurations.put("realtime-post-metrics", defaultConfig.entryTtl(Duration.ofSeconds(realtimePostMetricsTtl)));
        cacheConfigurations.put("realtime-account-metrics", defaultConfig.entryTtl(Duration.ofSeconds(realtimeAccountMetricsTtl)));
        cacheConfigurations.put("comments", defaultConfig.entryTtl(Duration.ofSeconds(commentsTtl)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
