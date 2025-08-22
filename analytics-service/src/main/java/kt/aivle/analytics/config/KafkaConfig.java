package kt.aivle.analytics.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;

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
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    @Bean
    public ConsumerFactory<String, SnsPostEvent> postEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            getCommonConsumerProps(), 
            new StringDeserializer(), 
            new JsonDeserializer<>(SnsPostEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SnsPostEvent> postEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SnsPostEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(postEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SnsAccountEvent> snsAccountEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            getCommonConsumerProps(), 
            new StringDeserializer(), 
            new JsonDeserializer<>(SnsAccountEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SnsAccountEvent> snsAccountEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SnsAccountEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(snsAccountEventConsumerFactory());
        return factory;
    }
}
