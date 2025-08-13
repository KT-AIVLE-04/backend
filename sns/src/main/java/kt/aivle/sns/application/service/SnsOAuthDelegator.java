package kt.aivle.sns.application.service;

import kt.aivle.sns.application.port.in.SnsOAuthUseCase;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SnsOAuthDelegator {

    private final Map<SnsType, SnsOAuthUseCase> serviceMap;

    public SnsOAuthDelegator(List<SnsOAuthUseCase> services) {
        this.serviceMap = new EnumMap<>(SnsType.class);
        for(SnsOAuthUseCase service : services) {
            serviceMap.put(service.supportSnsType(), service);
        }
    }

    public String getAuthUrl(SnsType type, Long userId) {
        return serviceMap.get(type).getAuthUrl(userId);
    }

    public void handleCallback(SnsType type, Long userId, String code) throws Exception {
        serviceMap.get(type).handleCallback(userId, code);
    }
}
