package kt.aivle.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import kt.aivle.auth.adapter.out.oauth2.OAuth2Adapter;
import kt.aivle.auth.application.port.out.OAuth2Port;
import kt.aivle.auth.application.service.CustomOAuth2UserService;
import kt.aivle.auth.properties.OAuth2Properties;

@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2Config {
    
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(CustomOAuth2UserService customOAuth2UserService) {
        return customOAuth2UserService;
    }
    
    @Bean
    public OAuth2Port<kt.aivle.auth.adapter.out.oauth2.dto.OAuth2UserInfo> oAuth2Port(OAuth2Adapter oAuth2Adapter) {
        return oAuth2Adapter;
    }
} 