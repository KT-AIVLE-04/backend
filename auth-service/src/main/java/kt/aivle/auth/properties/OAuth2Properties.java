package kt.aivle.auth.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuth2Properties {
    
    private Map<String, Registration> registration;
    private Map<String, Provider> provider;
    
    @Getter
    @Setter
    public static class Registration {
        private String clientId;
        private String clientSecret;
        private String[] scope;
        private String redirectUri;
    }
    
    @Getter
    @Setter
    public static class Provider {
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private String userNameAttribute;
    }
    
    public Registration getRegistration(String provider) {
        return registration.get(provider.toLowerCase());
    }
    
    public Provider getProvider(String provider) {
        return this.provider.get(provider.toLowerCase());
    }
    
    // 기존 메서드들도 유지 (하위 호환성)
    public Registration getGoogle() {
        return getRegistration("google");
    }
    
    public Provider getGoogleProvider() {
        return getProvider("google");
    }
    
    public Registration getKakao() {
        return getRegistration("kakao");
    }
    
    public Provider getKakaoProvider() {
        return getProvider("kakao");
    }
} 