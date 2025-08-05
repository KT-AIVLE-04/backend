package kt.aivle.content.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Content Management API")
                        .description("AI 영상/이미지 콘텐츠 관리 시스템 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KT AIVLE")
                                .email("support@ktaivle.com")));
    }
}