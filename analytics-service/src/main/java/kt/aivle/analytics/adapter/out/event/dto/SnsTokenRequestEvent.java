package kt.aivle.analytics.adapter.out.event.dto;

public record SnsTokenRequestEvent(
    String requestId,
    String userId,
    String snsType
) {
}
