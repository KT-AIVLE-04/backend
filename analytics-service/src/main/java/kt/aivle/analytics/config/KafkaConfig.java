package kt.aivle.analytics.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import kt.aivle.analytics.adapter.in.event.dto.PostInfoRequestMessage;
import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;
import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 로깅 Serializer
    private class LoggingJsonSerializer extends JsonSerializer<Object> {
        @Override
        public byte[] serialize(String topic, Object data) {
            try {
                String json = objectMapper.writeValueAsString(data);
                log.info("📤 [KAFKA SERIALIZER] 토픽: {}, 데이터: {}", topic, json);
            } catch (Exception e) {
                log.warn("Kafka serializer 로깅 중 오류: {}", e.getMessage());
            }
            return super.serialize(topic, data);
        }
    }

    // 로깅 Deserializer
    private class LoggingJsonDeserializer<T> extends JsonDeserializer<T> {
        
        public LoggingJsonDeserializer(Class<T> targetType, boolean useTypeHeaders) {
            super(targetType, useTypeHeaders);
        }
        
        @Override
        public T deserialize(String topic, byte[] data) {
            try {
                String json = new String(data, StandardCharsets.UTF_8);
                log.info("📥 [KAFKA DESERIALIZER] 토픽: {}, 데이터: {}", topic, json);
            } catch (Exception e) {
                log.warn("Kafka deserializer 로깅 중 오류: {}", e.getMessage());
            }
            return super.deserialize(topic, data);
        }
    }

    private Map<String, Object> getCommonConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analytics-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LoggingJsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "kt.aivle.*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        // 그룹 ID 로깅
        log.info("🏷️ [KAFKA CONSUMER] 그룹 ID: {}", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        
        return props;
    }

    @Bean
    public ConsumerFactory<String, SnsPostEvent> postEventConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent");
        
        return new DefaultKafkaConsumerFactory<>(
            props, 
            new StringDeserializer(), 
            new JsonDeserializer<>(SnsPostEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SnsPostEvent> postEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SnsPostEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(postEventConsumerFactory());
        
        // 에러 처리 설정
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
            log.error("Error in kafka listener: {}", exception.getMessage(), exception);
        }, new FixedBackOff(1000L, 3L))); // 1초 간격으로 3번 재시도
        
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SnsAccountEvent> snsAccountEventConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent");
        
        return new DefaultKafkaConsumerFactory<>(
            props, 
            new StringDeserializer(), 
            new JsonDeserializer<>(SnsAccountEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SnsAccountEvent> snsAccountEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SnsAccountEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(snsAccountEventConsumerFactory());
        
        // 에러 처리 설정
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
            log.error("Error in kafka listener: {}", exception.getMessage(), exception);
        }, new FixedBackOff(1000L, 3L))); // 1초 간격으로 3번 재시도
        
        return factory;
    }

    /* ====== Post Info Request-Reply ====== */
    private Map<String, Object> getCommonProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LoggingJsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ProducerFactory<String, PostInfoRequestMessage> postInfoRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(getCommonProducerProps());
    }

    @Bean
    public ConsumerFactory<String, PostInfoResponseMessage> postInfoReplyConsumerFactory() {
        LoggingJsonDeserializer<PostInfoResponseMessage> valueDeserializer =
                new LoggingJsonDeserializer<>(PostInfoResponseMessage.class, false);
        valueDeserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, PostInfoResponseMessage> postInfoRepliesContainer(
            ConsumerFactory<String, PostInfoResponseMessage> cf
    ) {
        String replyTopic = "post-info.reply";
        String groupId = "analytics-post-info-replies-" + UUID.randomUUID();
        
        log.info("🏷️ [KAFKA REPLY CONSUMER] 토픽: {}, 그룹 ID: {}", replyTopic, groupId);
        
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PostInfoResponseMessage>();
        factory.setConsumerFactory(cf);
        var container = factory.createContainer(replyTopic);
        container.getContainerProperties().setGroupId(groupId);
        container.getContainerProperties().setMissingTopicsFatal(false);
        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, PostInfoRequestMessage, PostInfoResponseMessage> postInfoReplyingKafkaTemplate(
            ProducerFactory<String, PostInfoRequestMessage> pf,
            ConcurrentMessageListenerContainer<String, PostInfoResponseMessage> replies
    ) {
        var tpl = new ReplyingKafkaTemplate<>(pf, replies);
        tpl.setDefaultReplyTimeout(Duration.ofSeconds(30));
        return tpl;
    }
}
