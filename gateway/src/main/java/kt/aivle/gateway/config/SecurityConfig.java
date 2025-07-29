package kt.aivle.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.gateway.exception.GatewayErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

import static kt.aivle.gateway.config.ExcludePaths.EXCLUDE_PATHS;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(EXCLUDE_PATHS.toArray(new String[0])).permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                )
                .build();
    }

    // 401 커스텀 body 응답 핸들러
    private ServerAuthenticationEntryPoint unauthorizedEntryPoint() {
        return (exchange, ex) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            GatewayErrorCode errorCode = GatewayErrorCode.INVALID_TOKEN;
            String codeName = ex.getMessage();
            try {
                if (codeName != null) {
                    errorCode = GatewayErrorCode.valueOf(codeName);
                }
            } catch (Exception ignored) {
            }

            ApiResponse<Void> apiResponse = ApiResponse.of(errorCode, null);

            byte[] bytes;
            try {
                bytes = objectMapper.writeValueAsBytes(apiResponse);
            } catch (Exception e) {
                bytes = ("{\"isSuccess\":false,\"message\":\"응답 직렬화 실패\",\"result\":null,\"errors\":null}").getBytes();
            }
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        };
    }
}