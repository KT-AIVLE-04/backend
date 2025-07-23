package kt.aivle.auth.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record AuthResponse(String accessToken, long accessTokenExpiration) {
}
