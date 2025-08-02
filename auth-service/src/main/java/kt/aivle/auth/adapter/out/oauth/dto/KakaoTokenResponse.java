package kt.aivle.auth.adapter.out.oauth.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private Integer expiresIn;
    
    @JsonProperty("refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;
    
    @JsonProperty("scope")
    private String scope; // 카카오도 scope를 공백으로 구분된 문자열로 반환
    
    // scope 문자열을 List로 변환하는 메서드
    public List<String> getScopeList() {
        if (scope == null || scope.trim().isEmpty()) {
            return List.of();
        }
        return List.of(scope.split("\\s+"));
    }
} 