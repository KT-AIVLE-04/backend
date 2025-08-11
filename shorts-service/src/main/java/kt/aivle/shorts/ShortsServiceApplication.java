package kt.aivle.shorts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"kt.aivle.shorts", "kt.aivle.common"})
public class ShortsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortsServiceApplication.class, args);
    }
}
