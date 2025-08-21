package kt.aivle.sns.application.service.oauth;

import kt.aivle.sns.adapter.out.persistence.repository.JpaOAuthStateRepository;
import kt.aivle.sns.adapter.out.persistence.entity.OAuthStateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private final JpaOAuthStateRepository repository;

    public String issue(Long userId, Long storeId) {
        String raw = userId + ":" + storeId + ":" + UUID.randomUUID();
        String state = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        repository.save(OAuthStateEntity.builder()
                        .state(state).userId(userId).storeId(storeId)
                        .expiresAt(Instant.now().plusSeconds(600)).build());
        return state;
    }

    public Pair<Long, Long> consume(String state) {
        OAuthStateEntity e = repository.findById(state).orElseThrow(() -> new IllegalStateException("invalid state"));
        if(e.getExpiresAt().isBefore(Instant.now())) {
            repository.deleteById(state);
            throw new IllegalStateException("state expired");
        }
        repository.deleteById(state);
        return Pair.of(e.getUserId(), e.getStoreId());
    }
}
