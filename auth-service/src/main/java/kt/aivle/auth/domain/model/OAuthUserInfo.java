package kt.aivle.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {
    private String providerId;
    private String email;
    private String name;
    private String profileImageUrl; // 선택적
    private String phoneNumber;     // 선택적
    private OAuthProvider provider;
} 