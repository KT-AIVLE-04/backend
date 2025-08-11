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

    private final Map<String, Object> attributes;//OAuth 제공자별 사용자 속성 정보
    private final String name;  // OAuth 제공자 ID
    private final OAuthProvider provider;//OAuth 제공자 타입
    private final String email;//사용자 이메일
    private final String displayName;//사용자 표시 이름
    private final String profileImageUrl;//프로필 이미지 URL
    private final String phoneNumber;//전화번호
    private final OAuthToken token;//OAuth 토큰 정보

}