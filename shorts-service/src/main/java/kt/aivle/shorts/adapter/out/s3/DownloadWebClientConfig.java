package kt.aivle.shorts.adapter.out.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DownloadWebClientConfig {
    @Bean
    public WebClient downloadWebClient() {
        return WebClient.builder().build();
    }
}