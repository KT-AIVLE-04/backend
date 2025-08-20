package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import kt.aivle.sns.adapter.in.web.dto.SnsAccountResponse;
import kt.aivle.sns.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class YoutubeChannelListApi {

    private final YoutubeClientFactory youtubeClientFactory;

    private final SnsAccountRepositoryPort snsAccountRepositoryPort;

    public SnsAccountResponse getYoutubeChannelInfo(Long userId, Long storeId) {

        try {

            // userId 기반으로 인증된 YouTube 객체 생성
            YouTube youtube = youtubeClientFactory.youtube(userId, storeId);

            // 업데이트 요청 api 생성 및 실행
            YouTube.Channels.List request = youtube.channels()
                    .list("snippet,contentDetails,statistics,brandingSettings");
            ChannelListResponse response = request.setMine(true).execute();

            Channel channel = response.getItems().get(0);
            ChannelSnippet snippet = channel.getSnippet();
            ChannelStatistics statistics = channel.getStatistics();
            ChannelSettings settings = channel.getBrandingSettings().getChannel();


            // SnsAccount Info 저장
            SnsAccount account = snsAccountRepositoryPort.findByUserIdAndStoreIdAndSnsType(userId, storeId, SnsType.youtube)
                    .map(existing -> {
                        existing = SnsAccount.builder()
                                .id(existing.getId())
                                .userId(existing.getUserId())
                                .storeId(existing.getStoreId())
                                .snsType(SnsType.youtube)
                                .snsAccountId(channel.getId()) // 유튜브 채널 id
                                .snsAccountName(snippet.getTitle()) // 채널명
                                .snsAccountDescription(snippet.getDescription()) // 채널 설명
                                .snsAccountUrl(snippet.getCustomUrl()) // 채널 url
                                .follower(statistics.getSubscriberCount().intValue()) // 구독자 수
                                .postCount(statistics.getVideoCount().intValue()) // 업로드 동영상 수
                                .viewCount(statistics.getViewCount().intValue()) // 전체 동영상 조회수
                                .keywords(Arrays.asList(settings.getKeywords()))
                                .build();
                        return existing;
                    })
                    .orElse(SnsAccount.builder()
                            .userId(userId)
                            .storeId(storeId)
                            .snsType(SnsType.youtube)
                            .snsAccountId(channel.getId()) // 유튜브 채널 id
                            .snsAccountName(snippet.getTitle()) // 채널명
                            .snsAccountDescription(snippet.getDescription()) // 채널 설명
                            .snsAccountUrl(snippet.getCustomUrl()) // 채널 url
                            .follower(statistics.getSubscriberCount().intValue()) // 구독자 수
                            .postCount(statistics.getVideoCount().intValue()) // 업로드 동영상 수
                            .viewCount(statistics.getViewCount().intValue()) // 전체 동영상 조회수
                            .keywords(Arrays.asList(settings.getKeywords()))
                            .build());

            snsAccountRepositoryPort.save(account);

            return SnsAccountResponse.from(account);

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("채널정보 불러오기 실패", e);
        }
    }
}
