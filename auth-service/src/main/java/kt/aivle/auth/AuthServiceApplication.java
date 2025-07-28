package kt.aivle.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import kt.aivle.auth.properties.OAuth2Properties;

@SpringBootApplication(scanBasePackages = {"kt.aivle.auth", "kt.aivle.common"})
@EnableConfigurationProperties(OAuth2Properties.class)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
