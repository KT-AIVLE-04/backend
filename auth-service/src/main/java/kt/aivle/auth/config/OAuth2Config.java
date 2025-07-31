package kt.aivle.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import kt.aivle.auth.application.service.CustomOAuth2UserService;
import kt.aivle.auth.properties.OAuth2Properties;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
@RequiredArgsConstructor
public class OAuth2Config {
    
    private final CustomOAuth2UserService customOAuth2UserService;
    
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return new OAuth2UserService<OAuth2UserRequest, OAuth2User>() {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                return customOAuth2UserService.loadUser(userRequest).block();
            }
        };
    }
} 