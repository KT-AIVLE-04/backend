package kt.aivle.sns.application.service.youtube;

import kt.aivle.sns.adapter.out.youtube.YoutubeChannelListApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeChannelUpdateApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeSearchListApi;
import kt.aivle.sns.application.port.in.SnsAccountUseCase;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YoutubeChannelService implements SnsAccountUseCase {

    private final YoutubeChannelListApi youtubeChannelListApi;
    private final YoutubeChannelUpdateApi youtubeChannelUpdateApi;
    private final YoutubeSearchListApi youtubeSearchListApi;

    @Override
    public SnsType supportSnsType() {
        return SnsType.youtube;
    }

    @Override
    public void getSnsAccountInfo(String userId) {

        youtubeChannelListApi.getYoutubeChannelInfo(userId);

    }

    @Override
    public void updateSnsAccount(String userId, SnsAccountUpdateRequest request) {

        youtubeChannelUpdateApi.updateAccount(
                userId,
                request.getSnsAccountId(),
                request.getSnsAccountDescription(),
                request.getKeywords()
        );
    }

    @Override
    public void getPostList(String userId) {
        youtubeSearchListApi.getYoutubeMyVideoList(userId);
    }
}
