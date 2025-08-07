package kt.aivle.sns.application.service;

import kt.aivle.sns.application.port.in.SnsAccountUseCase;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SnsAccountDelegator {

    private final Map<SnsType, SnsAccountUseCase> snsAccountServiceMap;

    public SnsAccountDelegator(List<SnsAccountUseCase> services) {
        this.snsAccountServiceMap = new EnumMap<>(SnsType.class);
        for(SnsAccountUseCase service : services) {
            snsAccountServiceMap.put(service.supportSnsType(), service);
        }
    }

    public void getAccountInfo(SnsType type, String userId) {
        snsAccountServiceMap.get(type).getSnsAccountInfo(userId);
    }

    public void updateAccount(SnsType type, String userId, SnsAccountUpdateRequest request) {
        snsAccountServiceMap.get(type).updateSnsAccount(userId, request);
    }

    public void getPostList(SnsType type, String userId) {
        snsAccountServiceMap.get(type).getPostList(userId);
    }
}
