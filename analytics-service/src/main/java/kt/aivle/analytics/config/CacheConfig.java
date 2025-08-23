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
    
    @Value("${app.cache.ttl.post-metrics:300}")
    private long postMetricsTtl;
    
    @Value("${app.cache.ttl.account-metrics:300}")
    private long accountMetricsTtl;
    
    @Value("${app.cache.ttl.comments:120}")
    private long commentsTtl;
    
    @Value("${app.cache.ttl.emotion-analysis:600}")
    private long emotionAnalysisTtl;
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("캐시 설정 - post-metrics: {}초, account-metrics: {}초, comments: {}초, emotion-analysis: {}초", 
                postMetricsTtl, accountMetricsTtl, commentsTtl, emotionAnalysisTtl);
        
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(300)) // 기본 5분
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 캐시별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("post-metrics", defaultConfig.entryTtl(Duration.ofSeconds(postMetricsTtl)));
        cacheConfigurations.put("account-metrics", defaultConfig.entryTtl(Duration.ofSeconds(accountMetricsTtl)));
        cacheConfigurations.put("comments", defaultConfig.entryTtl(Duration.ofSeconds(commentsTtl)));
        cacheConfigurations.put("emotion-analysis", defaultConfig.entryTtl(Duration.ofSeconds(emotionAnalysisTtl)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
