package kt.aivle.analytics.config;

import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CacheConfig {

    @Value("${app.cache.ttl.post-metrics:300}")
    private long postMetricsTtl;

    @Value("${app.cache.ttl.account-metrics:300}")
    private long accountMetricsTtl;

    @Value("${app.cache.ttl.emotion-analysis:600}")
    private long emotionAnalysisTtl;

    @Value("${app.cache.ttl.realtime-post-metrics:60}")
    private long realtimePostMetricsTtl;

    @Value("${app.cache.ttl.realtime-account-metrics:60}")
    private long realtimeAccountMetricsTtl;

    @Value("${app.cache.ttl.history-comments:300}")
    private long historyCommentsTtl;

    @Value("${app.cache.ttl.realtime-comments:120}")
    private long realtimeCommentsTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("캐시 TTL 설정 - 히스토리: {}초, 실시간: {}초, 댓글: {}초",
                postMetricsTtl, realtimePostMetricsTtl, historyCommentsTtl);

        // Redis 연결 테스트
        try {
            connectionFactory.getConnection().ping();
            log.info("✅ Redis 연결 성공");
        } catch (Exception e) {
            log.error("❌ Redis 연결 실패: {}", e.getMessage());
        }

        // Jackson ObjectMapper 설정 (Java 8 시간 타입 지원)
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(java.time.LocalDateTime.class, 
            new LocalDateTimeSerializer(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        javaTimeModule.addDeserializer(java.time.LocalDateTime.class, 
            new LocalDateTimeDeserializer(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        objectMapper.registerModule(javaTimeModule);
        
        objectMapper = objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(300))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        // 캐시별 TTL 설정 (일괄 처리)
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            "post-metrics", defaultConfig.entryTtl(Duration.ofSeconds(postMetricsTtl)),
            "account-metrics", defaultConfig.entryTtl(Duration.ofSeconds(accountMetricsTtl)),
            "emotion-analysis", defaultConfig.entryTtl(Duration.ofSeconds(emotionAnalysisTtl)),
            "realtime-post-metrics", defaultConfig.entryTtl(Duration.ofSeconds(realtimePostMetricsTtl)),
            "realtime-account-metrics", defaultConfig.entryTtl(Duration.ofSeconds(realtimeAccountMetricsTtl)),
            "realtime-comments", defaultConfig.entryTtl(Duration.ofSeconds(realtimeCommentsTtl)),
            "history-comments", defaultConfig.entryTtl(Duration.ofSeconds(historyCommentsTtl))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
