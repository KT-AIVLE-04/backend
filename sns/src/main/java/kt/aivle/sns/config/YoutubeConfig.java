package kt.aivle.sns.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import kt.aivle.sns.adapter.out.youtube.SnsTokenStore;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class YoutubeConfig {

    private final SnsTokenStore tokenStore;

    public YoutubeConfig(SnsTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public YouTube createYoutubeClient(String userId) throws IOException, GeneralSecurityException {
        // SnsTokenStore에서 OAuth 인증 완료된 Credential을 가져옵니다.
        var credential = tokenStore.getCredential(userId, SnsType.youtube);

        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("sns-video-service").build();
    }

    public YouTubeAnalytics createYoutubeAnalyticsClient(String userId) throws IOException, GeneralSecurityException {
        var credential = tokenStore.getCredential(userId, SnsType.youtube);

        return new YouTubeAnalytics.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("sns-analytics-service").build();
    }
}
