package kt.aivle.auth.application.port.out;

public interface TokenBlacklistRepositoryPort {
    void addAccessTokenToBlacklist(String jti, long expirationMillis);

    boolean isBlacklisted(String jti);
}
