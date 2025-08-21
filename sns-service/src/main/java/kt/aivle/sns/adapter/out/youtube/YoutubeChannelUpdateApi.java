package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelBrandingSettings;
import com.google.api.services.youtube.model.ChannelSettings;
import kt.aivle.sns.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.sns.domain.model.SnsAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class YoutubeChannelUpdateApi {

    private final YoutubeClientFactory youtubeClientFactory;

    private final SnsAccountRepositoryPort snsAccountRepositoryPort;

    public void updateAccount(Long userId,
                              Long storeId,
                              String accountId,
                              String description,
                              String[] keywords) {

        try {
            // userId 기반으로 인증된 YouTube 객체 생성
            YouTube youtube = youtubeClientFactory.youtube(userId, storeId);

            // Define the Channel object, which will be uploaded as the request body.
            Channel channel = new Channel();

            // Add the id string property to the Channel object.
            channel.setId(accountId);

            // Add the brandingSettings object property to the Channel object.
            ChannelBrandingSettings brandingSettings = new ChannelBrandingSettings();
            ChannelSettings channelSettings = new ChannelSettings();
            channelSettings.setDescription(description);
            String strKeywords = String.join(",", keywords);
            channelSettings.setKeywords(strKeywords);
            brandingSettings.setChannel(channelSettings);
            channel.setBrandingSettings(brandingSettings);

            // Define and execute the API request
            YouTube.Channels.Update request = youtube.channels()
                    .update("brandingSettings", channel);
            Channel response = request.execute();
            System.out.println("업데이트된 Channel ID: " + response.getId()); // 유튜브 채널 id

            Optional<SnsAccount> optionalSnsAccount = snsAccountRepositoryPort.findBySnsAccountId(accountId);
            if(optionalSnsAccount.isPresent()) {
                SnsAccount snsAccount = optionalSnsAccount.get();
                snsAccount.setSnsAccountDescription(description);
                snsAccount.setKeywords(new ArrayList<>(Arrays.asList(keywords)));

                snsAccountRepositoryPort.save(snsAccount);
            }else {
                System.err.println("해당 videoId를 가진 게시물을 찾을 수 없습니다: " + accountId);
            }

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("YouTube 채널 업데이트 실패", e);
        }

    }

}
