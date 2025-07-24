package kt.aivle.auth.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record AuthResponse(String type, String accessToken, long accessTokenExpiration,
                           String refreshToken, long refreshTokenExpiration) {
}
