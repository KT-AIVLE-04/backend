package kt.aivle.snspost.adapter.out.persistence;

import kt.aivle.snspost.application.port.out.HashtagRepository;
import kt.aivle.snspost.domain.model.Hashtag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HashtagPersistenceAdapter implements HashtagRepository {

    private final HashtagJpaRepository hashtagJpaRepository;

    @Override
    public Hashtag save(Hashtag hashtag) {
        return hashtagJpaRepository.save(hashtag);
    }

    @Override
    public Optional<Hashtag> findById(Long id) {
        return hashtagJpaRepository.findById(id);
    }

    @Override
    public Optional<Hashtag> findByName(String name) {
        return hashtagJpaRepository.findByName(name);
    }

    @Override
    public List<Hashtag> findByNameContaining(String name) {
        return hashtagJpaRepository.findByNameContaining(name);
    }

    @Override
    public List<Hashtag> findTop10ByOrderByPostCountDesc() {
        return hashtagJpaRepository.findTop10ByOrderByPostCountDesc();
    }

    @Override
    public void deleteById(Long id) {
        hashtagJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return hashtagJpaRepository.existsByName(name);
    }
} 