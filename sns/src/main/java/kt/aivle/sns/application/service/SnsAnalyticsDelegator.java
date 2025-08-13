package kt.aivle.sns.application.service;

import kt.aivle.sns.application.port.in.SnsAnalyticsUseCase;
import kt.aivle.sns.adapter.in.web.dto.SnsAnalyticsRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.adapter.in.web.dto.YoutubeAnalyticsResponse;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SnsAnalyticsDelegator {

    private final Map<SnsType, SnsAnalyticsUseCase> snsAnalyticsServiceMap;

    public SnsAnalyticsDelegator(List<SnsAnalyticsUseCase> services) {
        this.snsAnalyticsServiceMap = new EnumMap<>(SnsType.class);
        for(SnsAnalyticsUseCase service : services) {
            snsAnalyticsServiceMap.put(service.supportSnsType(), service);
        }
    }

    public YoutubeAnalyticsResponse getAnalytics(SnsType type, Long userId, SnsAnalyticsRequest request) {
        return snsAnalyticsServiceMap.get(type).getAnalytics(userId, request);
    }
}
