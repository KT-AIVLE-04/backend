package kt.aivle.sns.application.service;

import kt.aivle.sns.adapter.in.web.dto.SnsAccountResponse;
import kt.aivle.sns.application.port.in.SnsAccountUseCase;
import kt.aivle.sns.adapter.in.web.dto.SnsAccountUpdateRequest;
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

    public SnsAccountResponse getAccountInfo(SnsType type, Long userId, Long storeId) {
        return snsAccountServiceMap.get(type).getSnsAccountInfo(userId, storeId);
    }

    public void updateAccount(SnsType type, Long userId, SnsAccountUpdateRequest request) {
        snsAccountServiceMap.get(type).updateSnsAccount(userId, request);
    }

    public void getPostList(SnsType type, Long userId, Long storeId) {
        snsAccountServiceMap.get(type).getPostList(userId, storeId);
    }
}
