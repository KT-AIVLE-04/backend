package kt.aivle.sns.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthContext {
    private final Long userId;
    private final Long storeId;
}
