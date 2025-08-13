package kt.aivle.analytics.adapter.out.event;

import lombok.Builder;

@Builder
public record SnsTokenResponseEvent(
    String requestId,
    String userId,
    String snsType,
    String accessToken,
    String refreshToken,
    Long expiresAt,
    Boolean isExpired
) {
}
