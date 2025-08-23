package kt.aivle.analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 타임아웃 설정 
        factory.setConnectTimeout(30000);  // 연결 타임아웃: 30초
        factory.setReadTimeout(300000);    // 읽기 타임아웃: 5분 (300초)
        
        return new RestTemplate(factory);
    }
}
