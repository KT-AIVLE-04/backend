package kt.aivle.content.repository;

import kt.aivle.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByUserIdAndStoreIdOrderByCreatedAtDesc(Long storeId, Long userId);

    List<Content> findByUserIdAndStoreIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long userId,
            Long storeId,
            String title
    );
}