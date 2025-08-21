package kt.aivle.sns.application.service.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import kt.aivle.sns.application.port.in.TokenRefresher;
import kt.aivle.sns.config.YoutubeOAuthProperties;
import kt.aivle.sns.domain.model.RefreshedToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeTokenRefresher implements TokenRefresher {

    private final YoutubeOAuthProperties properties;

    private final JsonFactory JSON_FACTORY = new GsonFactory();

    private static final long DEFAULT_TTL_MS = 3600_000L; // 1h
    private static final long SKEW_MS = 60_000L;

    @Override
    public boolean supports(SnsType type) {
        return type == SnsType.youtube;
    }

    @Override
    public RefreshedToken refresh(String clientId, String clientSecret, String refreshToken) {

        try {
            HttpTransport httpTransport = new NetHttpTransport();

            GoogleTokenResponse resp = new GoogleRefreshTokenRequest(
                    httpTransport,
                    JSON_FACTORY,
                    refreshToken,
                    properties.getClientId(),
                    properties.getClientSecret()
            ).execute();

            long ttl = (resp.getExpiresInSeconds() != null ? resp.getExpiresInSeconds() * 1000L : DEFAULT_TTL_MS);
            long expiresAt = System.currentTimeMillis() + ttl - SKEW_MS;

            // 구글이 새 리프레시 토큰을 반환하지 않을 수 있으므로 null 체크
            String newRefreshToken = resp.getRefreshToken() != null ? resp.getRefreshToken() : refreshToken;

            return new RefreshedToken(resp.getAccessToken(), expiresAt, newRefreshToken);
        } catch (IOException e) {
            throw new IllegalStateException("YouTube 토큰 리프레시 실패", e);
        }
    }
}
