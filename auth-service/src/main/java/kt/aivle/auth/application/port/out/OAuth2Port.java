package kt.aivle.auth.application.port.out;

import java.util.Map;

public interface OAuth2Port<T> {
    
    /**
     * Authorization Code를 Access Token으로 교환
     */
    String exchangeCodeForToken(String provider, String code);
    
    /**
     * Access Token으로 사용자 정보 조회
     */
    T getUserInfo(String provider, String accessToken);
    
    /**
     * OAuth2 인증 URL 생성
     */
    String generateAuthUrl(String provider);
    
    /**
     * Map 형태의 attributes를 OAuth2UserInfo로 변환
     * Spring Security OAuth2에서 받은 attributes를 처리할 때 사용
     */
    T createUserInfoFromAttributes(String provider, Map<String, Object> attributes);
} 