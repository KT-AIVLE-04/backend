package kt.aivle.analytics.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

import kt.aivle.analytics.adapter.in.event.dto.PostInfoRequestMessage;
import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;
import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;
import kt.aivle.analytics.application.messaging.Topics;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;
    





    private Map<String, Object> getCommonConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analytics-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
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
        
        // AI 서비스는 시간이 오래 걸리므로 재시도 로직 제거
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
            log.error("Error in kafka listener: {}", exception.getMessage(), exception);
        }));
        
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
        
        // AI 서비스는 시간이 오래 걸리므로 재시도 로직 제거
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
            log.error("Error in kafka listener: {}", exception.getMessage(), exception);
        }));
        
        return factory;
    }

    /* ====== Post Info Request-Reply ====== */
    private Map<String, Object> getCommonProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ProducerFactory<String, PostInfoRequestMessage> postInfoRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(getCommonProducerProps());
    }

    @Bean
    public ConsumerFactory<String, PostInfoResponseMessage> postInfoReplyConsumerFactory() {
        JsonDeserializer<PostInfoResponseMessage> valueDeserializer =
                new JsonDeserializer<>(PostInfoResponseMessage.class, false);
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
        String replyTopic = Topics.POST_INFO_REPLY;
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

    /* ====== Saga 이벤트용 Producer/Consumer 설정 ====== */
    @Bean
    public ProducerFactory<String, Object> sagaProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // At-Least-Once 전송 보장 설정
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // 멱등성 보장 설정
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public org.springframework.kafka.core.KafkaTemplate<String, Object> sagaKafkaTemplate() {
        return new org.springframework.kafka.core.KafkaTemplate<>(sagaProducerFactory());
    }
}
