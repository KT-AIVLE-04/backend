package kt.aivle.sns;

import kt.aivle.sns.config.YoutubeOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"kt.aivle.sns", "kt.aivle.common"})
@EnableConfigurationProperties({YoutubeOAuthProperties.class})  // 다른 sns properties 추가
public class SnsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnsServiceApplication.class, args);
    }
}