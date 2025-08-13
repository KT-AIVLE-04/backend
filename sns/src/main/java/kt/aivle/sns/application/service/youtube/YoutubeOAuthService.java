package kt.aivle.sns.application.service.youtube;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import kt.aivle.sns.application.port.in.SnsOAuthUseCase;
import kt.aivle.sns.config.YoutubeOAuthProperties;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeOAuthService implements SnsOAuthUseCase {
    private static final List<String> SCOPES = List.of(
            // video.insert
            "https://www.googleapis.com/auth/youtube.upload",
            // video.update, video.delete, channel.update
            "https://www.googleapis.com/auth/youtube.force-ssl",
            // channel.list, youtubeAnalyticsService.reports
            "https://www.googleapis.com/auth/youtube.readonly");

    private final YoutubeOAuthProperties properties;

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final YoutubeTokenService youtubeTokenService;

    @Override
    public SnsType supportSnsType() {
        return SnsType.youtube;
    }

    @Override
    public String getAuthUrl(Long userId) {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    properties.getClientId(),
                    properties.getClientSecret(),
                    SCOPES
            ).setAccessType("offline").build();

            return flow.newAuthorizationUrl()
                    .setRedirectUri(properties.getRedirectUri())
                    .set("prompt", "consent")   // refresh token 새로 발급 받게
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Youtube auth URL", e);
        }
    }

    @Override
    public void handleCallback(Long userId, String code) throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        TokenResponse tokens =  new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                JSON_FACTORY,
                properties.getTokenUri(),
                properties.getClientId(),
                properties.getClientSecret(),
                code,
                properties.getRedirectUri()
        ).execute();

        youtubeTokenService.saveToken(
                userId,
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                tokens.getExpiresInSeconds());
    }
}
