package kt.aivle.common.kafka;

import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoRequestMessage;
import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoResponseMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaEventConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    /* ========= 공통 Producer (모든 일반 이벤트 발행에 사용) ========= */
    @Bean
    public ProducerFactory<String, Object> objectProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> eventKafkaTemplate(
            ProducerFactory<String, Object> objectProducerFactory
    ) {
        return new KafkaTemplate<>(objectProducerFactory);
    }

    /* ========= store request–reply 전용 빈 ========= */
    @Bean
    public ConsumerFactory<String, StoreInfoResponseMessage> storeReplyConsumerFactory() {
        JsonDeserializer<StoreInfoResponseMessage> valueDeserializer =
                new JsonDeserializer<>(StoreInfoResponseMessage.class, false);
        valueDeserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, StoreInfoResponseMessage> storeRepliesContainer(
            ConsumerFactory<String, StoreInfoResponseMessage> storeReplyConsumerFactory,
            @Value("${topic.store.reply}") String replyTopic
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, StoreInfoResponseMessage>();
        factory.setConsumerFactory(storeReplyConsumerFactory);

        var container = factory.createContainer(replyTopic);
        container.getContainerProperties().setGroupId("shorts-replies-" + UUID.randomUUID());
        container.getContainerProperties().setMissingTopicsFatal(false);
        return container;
    }

    @Bean
    public ProducerFactory<String, StoreInfoRequestMessage> storeRequestProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ReplyingKafkaTemplate<String, StoreInfoRequestMessage, StoreInfoResponseMessage> storeReplyingKafkaTemplate(
            ProducerFactory<String, StoreInfoRequestMessage> storeRequestProducerFactory,
            ConcurrentMessageListenerContainer<String, StoreInfoResponseMessage> storeRepliesContainer
    ) {
        var template = new ReplyingKafkaTemplate<>(storeRequestProducerFactory, storeRepliesContainer);
        template.setDefaultReplyTimeout(Duration.ofSeconds(10));
        return template;
    }
}