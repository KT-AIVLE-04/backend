package kt.aivle.auth.domain.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth 사용자 정보를 담는 통합 모델
 */
@Getter
@Builder
public class OAuthUser {
    
    private final Map<String, Object> attributes;
    private final String name;  // OAuth 제공자 ID
    private final OAuthProvider provider;
    private final String email;
    private final String displayName;
    private final String profileImageUrl;
    private final String phoneNumber;
    private final OAuthToken token;
    
    /**
     * OAuth 제공자별 사용자 속성 정보
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    /**
     * OAuth 제공자에서 제공하는 고유 식별자
     */
    public String getName() {
        return name;
    }
    
    /**
     * OAuth 제공자 타입
     */
    public OAuthProvider getProvider() {
        return provider;
    }
    
    /**
     * 사용자 이메일
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * 사용자 표시 이름
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 프로필 이미지 URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    /**
     * 전화번호
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * OAuth 토큰 정보
     */
    public OAuthToken getToken() {
        return token;
    }
} 