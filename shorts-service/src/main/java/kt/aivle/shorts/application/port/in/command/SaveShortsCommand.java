package kt.aivle.shorts.application.port.in.command;

public record SaveShortsCommand(Long userId, Long storeId, String key) {
}