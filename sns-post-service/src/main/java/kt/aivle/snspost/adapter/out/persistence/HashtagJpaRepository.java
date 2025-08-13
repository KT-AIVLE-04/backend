package kt.aivle.snspost.adapter.out.persistence;

import kt.aivle.snspost.domain.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagJpaRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    List<Hashtag> findByNameContaining(String name);

    List<Hashtag> findTop10ByOrderByPostCountDesc();

    boolean existsByName(String name);
} 