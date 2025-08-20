package kt.aivle.sns.application.service.youtube;

import kt.aivle.sns.adapter.in.web.dto.SnsAccountResponse;
import kt.aivle.sns.adapter.out.youtube.YoutubeChannelListApi;
import kt.aivle.sns.application.event.SnsAccountEvent;
import kt.aivle.sns.application.port.in.AccountSyncUseCase;
import kt.aivle.sns.application.port.out.EventPublisherPort;
import kt.aivle.sns.application.service.oauth.OAuthStateService;
import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeAccountSyncService implements AccountSyncUseCase {

    private final YoutubeChannelListApi youtubeChannelListApi;
    private final YoutubeTokenService tokenService;
    private final EventPublisherPort publisher;

    @Override
    public SnsType supportSnsType() {
        return SnsType.youtube;
    }

    @Override
    public void accountSync(SnsType snsType, Long userId, Long storeId) {
        SnsAccountResponse response = youtubeChannelListApi.getYoutubeChannelInfo(userId, storeId);

        if (response == null) return;


        SnsAccountEvent e = SnsAccountEvent.builder()
                .id(response.id())
                .userId(response.userId())
                .snsAccountId(response.snsAccountId())
                .type(SnsType.youtube)
                .build();

        log.warn("e = {}", e);

        publisher.publishSnsAccountConnected(e);
    }
}
