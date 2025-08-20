package kt.aivle.sns.application.service;

import kt.aivle.sns.adapter.in.web.SnsOAuthController;
import kt.aivle.sns.application.port.in.AccountSyncUseCase;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountSyncDelegator {

    private final Map<SnsType, AccountSyncUseCase> serviceMap;

    public AccountSyncDelegator(List<AccountSyncUseCase> services) {
        this.serviceMap = new EnumMap<>(SnsType.class);
        for(AccountSyncUseCase service : services) {
            serviceMap.put(service.supportSnsType(), service);
        }
    }

    public void accountSync(SnsType type, Long userId, Long storeId) {
        serviceMap.get(type).accountSync(type, userId, storeId);
    }
}
