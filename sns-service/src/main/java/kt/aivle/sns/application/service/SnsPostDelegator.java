package kt.aivle.sns.application.service;

import kt.aivle.sns.adapter.in.web.dto.request.PostCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.request.PostDeleteRequest;
import kt.aivle.sns.adapter.in.web.dto.request.PostUpdateRequest;
import kt.aivle.sns.adapter.in.web.dto.response.PostResponse;
import kt.aivle.sns.application.port.in.SnsPostUseCase;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SnsPostDelegator {

    private final Map<SnsType, SnsPostUseCase> snsPostServiceMap;

    public SnsPostDelegator(List<SnsPostUseCase> services) {
        this.snsPostServiceMap = new EnumMap<>(SnsType.class);
        for (SnsPostUseCase service : services) {
            snsPostServiceMap.put(service.supportSnsType(), service);
        }
    }

    public PostResponse upload(Long userId, Long storeId, PostCreateRequest request) {
        return snsPostServiceMap.get(SnsType.valueOf(request.snsType())).upload(userId, storeId, request);
    }

    public PostResponse update(Long userId, Long storeId, Long postId, PostUpdateRequest request) {
        return snsPostServiceMap.get(SnsType.valueOf(request.snsType())).update(userId, storeId, postId, request);
    }

    public void delete(Long userId, Long storeId, Long postId, PostDeleteRequest request) {
        snsPostServiceMap.get(SnsType.valueOf(request.snsType())).delete(userId, storeId, postId, request);
    }
}
