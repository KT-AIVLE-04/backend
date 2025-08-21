package kt.aivle.sns.application.service.youtube;

import kt.aivle.sns.adapter.in.web.dto.response.SnsAccountResponse;
import kt.aivle.sns.adapter.out.youtube.YoutubeChannelListApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeChannelUpdateApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeSearchListApi;
import kt.aivle.sns.application.port.in.SnsAccountUseCase;
import kt.aivle.sns.adapter.in.web.dto.request.SnsAccountUpdateRequest;
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
    public SnsAccountResponse getSnsAccountInfo(Long userId, Long storeId) {

        return youtubeChannelListApi.getYoutubeChannelInfo(userId, storeId);

    }

    @Override
    public void updateSnsAccount(Long userId, SnsAccountUpdateRequest request) {

        youtubeChannelUpdateApi.updateAccount(
                userId,
                request.getStoreId(),
                request.getSnsAccountId(),
                request.getSnsAccountDescription(),
                request.getKeywords()
        );
    }

    @Override
    public void getPostList(Long userId, Long storeId) {
        youtubeSearchListApi.getYoutubeMyVideoList(userId, storeId);
    }
}
