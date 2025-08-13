package kt.aivle.snspost.application.port.out;

import kt.aivle.snspost.domain.model.Hashtag;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository {

    Hashtag save(Hashtag hashtag);

    Optional<Hashtag> findById(Long id);

    Optional<Hashtag> findByName(String name);

    List<Hashtag> findByNameContaining(String name);

    List<Hashtag> findTop10ByOrderByPostCountDesc();

    void deleteById(Long id);

    boolean existsByName(String name);
} 