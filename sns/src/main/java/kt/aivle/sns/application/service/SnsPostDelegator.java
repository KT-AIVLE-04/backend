package kt.aivle.sns.application.service;

import kt.aivle.sns.application.port.in.SnsPostUseCase;
import kt.aivle.sns.domain.model.PostDeleteRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.domain.model.PostUpdateRequest;
import kt.aivle.sns.domain.model.PostUploadRequest;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SnsPostDelegator {

    private final Map<SnsType, SnsPostUseCase> snsPostServiceMap;

    public SnsPostDelegator(List<SnsPostUseCase> services) {
        this.snsPostServiceMap = new EnumMap<>(SnsType.class);
        for(SnsPostUseCase service : services) {
            snsPostServiceMap.put(service.supportSnsType(), service);
        }
    }

    public void upload(SnsType type, String userId, PostUploadRequest request) {
        snsPostServiceMap.get(type).upload(userId, request);
    }

    public void update(SnsType type, String userId, PostUpdateRequest request) {
        snsPostServiceMap.get(type).update(userId, request);
    }

    public void delete(SnsType type, String userId, PostDeleteRequest request) {
        snsPostServiceMap.get(type).delete(userId, request);
    }
}
