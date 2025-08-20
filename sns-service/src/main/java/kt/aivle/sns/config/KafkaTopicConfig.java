package kt.aivle.sns.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public org.apache.kafka.clients.admin.NewTopic snsAccountConnected() {
        return org.springframework.kafka.config.TopicBuilder.name("sns-account.connected")
                .partitions(3).replicas(1).build();
    }
}
