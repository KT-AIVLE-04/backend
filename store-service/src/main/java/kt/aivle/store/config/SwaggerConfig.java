package kt.aivle.store.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(new Info().title("Store Service API").version("v0.0.1"))
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes("bearerAuth", securityScheme)
                )
                .security(List.of(securityRequirement));
    }

    @Bean
    public OpenApiCustomizer removeUserIdHeader() {
        return openApi -> {
            openApi.getPaths().values()
                    .forEach(pathItem ->
                            pathItem.readOperations()
                                    .forEach(op -> {
                                        List<io.swagger.v3.oas.models.parameters.Parameter> params = op.getParameters();
                                        if (params != null) {
                                            params.removeIf(p -> "X-USER-ID".equals(p.getName()));
                                        }
                                    })
                    );
        };
    }
}