package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import kt.aivle.sns.adapter.in.web.dto.SnsAnalyticsRequest;
import kt.aivle.sns.adapter.in.web.dto.YoutubeAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeAnalyticsApi {

    private final YoutubeClientFactory youtubeClientFactory;

    public YoutubeAnalyticsResponse getYoutubeAnalytics(Long userId, SnsAnalyticsRequest apirequest) {

        try {

            // userId 기반으로 인증된 YouTube 객체 생성
            YouTubeAnalytics youtubeAnalytics = youtubeClientFactory.analytics(userId);

            // Define and execute the API request
            YouTubeAnalytics.Reports.Query request = youtubeAnalytics.reports()
                    .query();
            QueryResponse response = request.setDimensions("video,day") // 날짜별 동영상의 지정한 metrics 측정
                    .setEndDate(apirequest.getEndDate())
                    .setFilters("video=="+apirequest.getPostId())
                    .setIds("channel==MINE")
                    .setMetrics("views,comments,likes,dislikes,estimatedMinutesWatched,shares") // 측정항목
                    .setStartDate(apirequest.getStartDate())
                    .execute();
            System.out.println(response);

            return YoutubeAnalyticsResponse.from(response);


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
