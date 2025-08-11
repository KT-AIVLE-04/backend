package kt.aivle.store.adapter.in.event;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, StoreInfoRequestMessage> requestConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrap
    ) {
        JsonDeserializer<StoreInfoRequestMessage> valueDeserializer =
                new JsonDeserializer<>(StoreInfoRequestMessage.class, false);
        valueDeserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StoreInfoRequestMessage> requestListenerFactory(
            ConsumerFactory<String, StoreInfoRequestMessage> requestConsumerFactory,
            KafkaTemplate<String, StoreInfoResponseMessage> replyKafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, StoreInfoRequestMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(requestConsumerFactory);
        factory.setReplyTemplate(replyKafkaTemplate);
        return factory;
    }

    @Bean
    public ProducerFactory<String, StoreInfoResponseMessage> replyProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrap
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, StoreInfoResponseMessage> replyKafkaTemplate(
            ProducerFactory<String, StoreInfoResponseMessage> pf
    ) {
        return new KafkaTemplate<>(pf);
    }
}
