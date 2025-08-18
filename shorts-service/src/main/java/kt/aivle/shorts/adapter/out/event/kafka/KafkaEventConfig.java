package kt.aivle.shorts.adapter.out.event.kafka;

import kt.aivle.shorts.adapter.out.event.contents.Ack;
import kt.aivle.shorts.adapter.out.event.contents.CreateContentRequestMessage;
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

    private Map<String, Object> commonProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    /* ====== store request-reply====== */
    @Bean
    public ProducerFactory<String, StoreInfoRequestMessage> storeRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerProps());
    }

    @Bean
    public ConsumerFactory<String, StoreInfoResponseMessage> storeReplyConsumerFactory() {
        JsonDeserializer<StoreInfoResponseMessage> vd =
                new JsonDeserializer<>(StoreInfoResponseMessage.class, false);
        vd.addTrustedPackages("*");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), vd);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, StoreInfoResponseMessage> storeRepliesContainer(
            ConsumerFactory<String, StoreInfoResponseMessage> cf,
            @Value("${topic.store.reply}") String replyTopic
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, StoreInfoResponseMessage>();
        factory.setConsumerFactory(cf);
        var container = factory.createContainer(replyTopic);
        container.getContainerProperties().setGroupId("shorts-store-replies-" + UUID.randomUUID());
        container.getContainerProperties().setMissingTopicsFatal(false);
        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, StoreInfoRequestMessage, StoreInfoResponseMessage> storeReplyingKafkaTemplate(
            ProducerFactory<String, StoreInfoRequestMessage> pf,
            ConcurrentMessageListenerContainer<String, StoreInfoResponseMessage> replies
    ) {
        var tpl = new ReplyingKafkaTemplate<>(pf, replies);
        tpl.setDefaultReplyTimeout(Duration.ofSeconds(10));
        return tpl;
    }

    /* ====== content request-reply (Ack) ====== */
    @Bean
    public ProducerFactory<String, CreateContentRequestMessage> contentRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerProps());
    }

    @Bean
    public ConsumerFactory<String, Ack> contentReplyConsumerFactory() {
        JsonDeserializer<Ack> vd = new JsonDeserializer<>(Ack.class, false);
        vd.addTrustedPackages("*");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), vd);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Ack> contentRepliesContainer(
            ConsumerFactory<String, Ack> cf,
            @Value("${topic.content.reply}") String replyTopic
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Ack>();
        factory.setConsumerFactory(cf);
        var container = factory.createContainer(replyTopic);
        container.getContainerProperties().setGroupId("shorts-content-replies-" + UUID.randomUUID());
        container.getContainerProperties().setMissingTopicsFatal(false);
        return container;
    }

    @Bean
    public ReplyingKafkaTemplate<String, CreateContentRequestMessage, Ack> contentReplyingKafkaTemplate(
            ProducerFactory<String, CreateContentRequestMessage> pf,
            ConcurrentMessageListenerContainer<String, Ack> replies
    ) {
        var tpl = new ReplyingKafkaTemplate<>(pf, replies);
        tpl.setDefaultReplyTimeout(Duration.ofSeconds(30));
        return tpl;
    }

    /* ====== 일반 이벤트 발행용 KafkaTemplate(Object) ====== */
    @Bean
    public ProducerFactory<String, Object> objectProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerProps());
    }

    @Bean
    public KafkaTemplate<String, Object> eventKafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}