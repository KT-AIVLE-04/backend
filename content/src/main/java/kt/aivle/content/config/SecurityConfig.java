package kt.aivle.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security 설정 클래스
 *
 * 현재는 기본적인 설정만 포함
 * 실제 프로젝트에서는 JWT, OAuth2 등 인증/인가 설정 추가 필요
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API이므로)
                .csrf(csrf -> csrf.disable()) // 변경된 부분

                // CORS 설정 (WebConfig에서 설정한 것 적용)
                .cors(cors -> {}) // 변경된 부분 (람다식 사용)

                // 세션 정책 설정 (REST API이므로 STATELESS)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // 변경된 부분

                // HTTP 요청 인가 설정
                .authorizeHttpRequests(authz -> authz
                        // 공개 엔드포인트
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger 관련 (개발환경)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 정적 리소스
                        .requestMatchers("/uploads/**").permitAll()

                        // 나머지 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 기타 모든 요청 허용 (개발 단계)
                        .anyRequest().permitAll()
                )
                // 보안 헤더 설정
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny()) // 변경된 부분
                        .contentTypeOptions(contentTypeOptions -> {}) // 변경된 부분
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                        )
                        .referrerPolicy(referrerPolicy -> referrerPolicy
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        ) // 변경된 부분
                );

        return http.build();
    }
}