package kt.aivle.auth.domain.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthToken {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private List<String> scope;
    private Integer refreshTokenExpiresIn;
} 