package kt.aivle.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService, OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Gateway에서 이미 인증 처리하므로 모든 요청 허용
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/api/oauth2/authorization")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/api/login/oauth2/code/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
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
                    if(errorRedirectUrl == null) {
                        response.sendRedirect("/error?message=Invalid redirect URL");
                        return;
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
