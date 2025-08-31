package kt.aivle.analytics.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 타임아웃 설정 - AI 서비스는 오래 걸리므로 타임아웃 없앰
        factory.setConnectTimeout(60000);  // 연결 타임아웃: 60초
        factory.setReadTimeout(600000);         // 읽기 타임아웃: 10분 (600초)
        
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
        
        // 로깅 인터셉터 추가
        restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                    org.springframework.http.client.ClientHttpRequestExecution execution) throws IOException {
                
                log.info("🌐 HTTP 요청: {} {}", request.getMethod(), request.getURI());
                log.info("📤 요청 헤더: {}", request.getHeaders());
                if (body.length > 0) {
                    log.info("📤 요청 본문: {}", new String(body, StandardCharsets.UTF_8));
                }
                
                ClientHttpResponse response = execution.execute(request, body);
                
                log.info("📥 응답 상태: {}", response.getStatusCode());
                log.info("📥 응답 헤더: {}", response.getHeaders());
                
                // 응답 본문 읽기 (한 번만 읽기)
                byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
                String responseBodyStr = new String(responseBody, StandardCharsets.UTF_8);
                log.info("📥 응답 본문: {}", responseBodyStr);
                
                // 원본 응답 반환 (이미 읽었으므로 다시 읽을 수 없음)
                return response;
            }
        });
        
        return restTemplate;
    }
}
