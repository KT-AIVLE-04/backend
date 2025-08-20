package kt.aivle.sns.adapter.in.web.dto;

import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import lombok.Builder;

import java.util.List;

@Builder
public record SnsAccountResponse (
      Long id,
      Long userId,
      Long storeId,
      SnsType snsType,
      String snsAccountId,
      String snsAccountName,
      String snsAccountDescription,
      String snsAccountUrl,
      Integer follower,
      Integer postCount,
      Integer viewCount,
      List<String> keyword
){
    public static SnsAccountResponse from(SnsAccount account) {
        return SnsAccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .storeId(account.getStoreId())
                .snsType(account.getSnsType())
                .snsAccountId(account.getSnsAccountId())
                .snsAccountName(account.getSnsAccountName())
                .snsAccountDescription(account.getSnsAccountDescription())
                .snsAccountUrl(account.getSnsAccountUrl())
                .follower(account.getFollower())
                .postCount(account.getPostCount())
                .viewCount(account.getViewCount())
                .keyword(account.getKeywords())
                .build();
    }
}
