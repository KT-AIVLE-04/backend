package kt.aivle.auth.adapter.out.oauth2.dto;

/**
 * OAuth2 사용자 정보를 위한 공통 인터페이스
 */
public interface OAuth2UserInfo {
    
    /**
     * 사용자의 고유 식별자 반환
     */
    String getProviderId();
    
    /**
     * 사용자의 이메일 반환
     */
    String getEmail();
    
    /**
     * 사용자의 이름 반환
     */
    String getName();
    
    /**
     * OAuth 제공자 이름 반환
     */
    String getProvider();
} 