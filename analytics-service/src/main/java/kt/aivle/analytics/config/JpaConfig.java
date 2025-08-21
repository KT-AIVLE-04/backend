package kt.aivle.analytics.config;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "kt.aivle.analytics.adapter.out.persistence")
public class JpaConfig {
    
    @PostConstruct
    public void init() {
        // 애플리케이션 전체의 기본 타임존을 Asia/Seoul로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.setProperty("user.timezone", "Asia/Seoul");
    }
    
    @Bean
    public ZoneId zoneId() {
        // 명시적으로 Asia/Seoul 타임존을 Bean으로 등록
        return ZoneId.of("Asia/Seoul");
    }
}
