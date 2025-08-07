package kt.aivle.sns.application.service.youtube;

import kt.aivle.sns.adapter.out.youtube.YoutubeAnalyticsApi;
import kt.aivle.sns.application.port.in.SnsAnalyticsUseCase;
import kt.aivle.sns.domain.model.SnsAnalyticsRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.domain.model.YoutubeAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YoutubeAnalyticsService implements SnsAnalyticsUseCase {

    private final YoutubeAnalyticsApi youtubeAnalyticsApi;

    @Override
    public SnsType supportSnsType() {
        return SnsType.youtube;
    }

    @Override
    public YoutubeAnalyticsResponse getAnalytics(String userId, SnsAnalyticsRequest request) {
        return youtubeAnalyticsApi.getYoutubeAnalytics(userId, request);
    }

}
