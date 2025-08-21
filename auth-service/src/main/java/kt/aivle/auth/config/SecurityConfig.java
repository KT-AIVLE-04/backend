package kt.aivle.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.application.service.OAuthService;
import kt.aivle.auth.domain.model.OAuthProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final OAuthService oAuthService;
    
    @Value("${client.url}")
    private String clientUrl;
    
    public SecurityConfig(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Gateway에서 이미 인증 처리하므로 모든 요청 허용
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/api/auth/oauth2/authorization")
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/api/auth/oauth2/code/*")
                )
                .successHandler((request, response, authentication) -> {
                    // 성공 시 JWT 토큰 발급
                    String requestUri = request.getRequestURI();
                    log.info("OAuth 성공 핸들러 호출: requestUri={}", requestUri);
                    
                    // URL에서 provider 추출
                    OAuthProvider provider = OAuthProvider.fromUrl(requestUri);
                    log.info("OAuth provider 선택: {}", provider);
                    
                    // OAuth 로그인 처리 및 JWT 토큰 생성
                    AuthResponse authResponse = oAuthService.oauthLogin((OAuth2User) authentication.getPrincipal(), provider);
                    log.info("OAuth 로그인 성공: provider={}, userId={}", provider, authResponse.accessToken().substring(0, 10) + "...");
                    
                    // Fragment URL 생성 및 리다이렉트
                    String redirectUrl = oAuthService.createOAuthRedirectUrl(authResponse, clientUrl);
                    log.info("클라이언트로 리다이렉트: {}", redirectUrl);
                    response.sendRedirect(redirectUrl);
                })
                .failureHandler((request, response, exception) -> {
                    // 실패 시 클라이언트로 에러 페이지 리다이렉트
                    log.error("OAuth 로그인 실패: {}", exception.getMessage());
                    response.sendRedirect(clientUrl + "/oauth-error?error=" + exception.getMessage());
                })
            );
        
        return http.build();
    }
}
