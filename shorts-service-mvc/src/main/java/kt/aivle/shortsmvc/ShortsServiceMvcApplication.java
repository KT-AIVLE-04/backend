package kt.aivle.shortsmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"kt.aivle.shortsmvc", "kt.aivle.common"})
public class ShortsServiceMvcApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortsServiceMvcApplication.class, args);
    }
}