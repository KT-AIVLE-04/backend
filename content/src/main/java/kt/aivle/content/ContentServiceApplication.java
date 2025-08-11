package kt.aivle.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.*;

@SpringBootApplication(scanBasePackages = {"kt.aivle.content", "kt.aivle.common"})
public class ContentServiceApplication {
    public static void main(String[] args) {
        run(ContentServiceApplication.class, args);
    }
}
