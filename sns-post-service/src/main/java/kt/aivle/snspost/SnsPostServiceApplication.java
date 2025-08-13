package kt.aivle.snspost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"kt.aivle.snspost", "kt.aivle.common"})
public class SNSPostServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SNSPostServiceApplication.class, args);
    }
}
