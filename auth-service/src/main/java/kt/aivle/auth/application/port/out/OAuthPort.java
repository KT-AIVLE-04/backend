package kt.aivle.auth.application.port.out;

import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.OAuthToken;
import kt.aivle.auth.domain.model.OAuthUserInfo;
import kt.aivle.auth.domain.model.User;

public interface OAuthPort {
    /**
     * OAuth 인증 - 인증코드로 액세스 토큰 발급
     */
    OAuthToken authenticate(String authorizationCode, OAuthProvider provider);
    
    /**
     * OAuth 사용자 정보 조회 - 액세스 토큰으로 사용자 정보 가져오기
     */
    OAuthUserInfo getUserInfo(String accessToken, OAuthProvider provider);
    
    /**
     * 소셜 회원 연결 또는 신규 가입 - OAuth 사용자 정보로 기존 회원과 연결하거나 새로 생성
     */
    User linkOrSignupUser(OAuthUserInfo oauthUserInfo);
} 