package kt.aivle.auth.config;

import static kt.aivle.common.config.AuthExcludePaths.EXCLUDE_PATHS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import kt.aivle.auth.application.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(EXCLUDE_PATHS.toArray(new String[0])).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/api/oauth2/authorization")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/api/login/oauth2/code/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    try {
                        customOAuth2UserService.handleOAuth2Success(request, response, authentication);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .failureHandler((request, response, exception) -> {
                    // OAuth2 로그인 실패 시 처리
                    String errorRedirectUrl = extractRedirectUrl(request);
                    if (errorRedirectUrl == null) {
                        errorRedirectUrl = "http://localhost:5173/auth/error"; // 기본값
                    }
                    response.sendRedirect(errorRedirectUrl + "?message=" + 
                        java.net.URLEncoder.encode(exception.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
                })
            );
        
        return http.build();
    }

    private String extractRedirectUrl(HttpServletRequest request) {
        // Origin 헤더에서 프론트엔드 URL 추출
        String origin = request.getHeader("Origin");
        if (origin != null) {
            return origin + "/auth/error";
        }
        
        return null;
    }
}
