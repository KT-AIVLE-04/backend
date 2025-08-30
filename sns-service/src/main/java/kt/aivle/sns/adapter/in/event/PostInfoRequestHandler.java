package kt.aivle.sns.adapter.in.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import kt.aivle.sns.adapter.in.event.dto.PostInfoRequestMessage;
import kt.aivle.sns.adapter.in.event.dto.PostInfoResponseMessage;
import kt.aivle.sns.adapter.out.persistence.repository.JpaPostRepository;
import kt.aivle.sns.domain.model.PostEntity;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.infra.CloudFrontSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostInfoRequestHandler {

    private final JpaPostRepository postRepository;
    private final CloudFrontSigner cloudFrontSigner;

    @KafkaListener(
            topics = "post-info.request",
            groupId = "sns-post-info-group",
            containerFactory = "postInfoRequestListenerFactory"
    )
    @SendTo
    public PostInfoResponseMessage handle(PostInfoRequestMessage request) {
        try {
            log.info("📨 [KAFKA] Received request - postId: {}", request.getPostId());
            
            PostEntity post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new RuntimeException("Post not found: " + request.getPostId()));
            
            // URL 생성
            String url = generateUrl(post);
            
            PostInfoResponseMessage response = PostInfoResponseMessage.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .description(post.getDescription())
                    .url(url)
                    .tags(post.getTags())
                    .publishAt(post.getPublishAt())
                    .snsPostId(post.getSnsPostId())
                    .snsType(post.getSnsType().name())
                    .build();
            
            log.info("📤 [KAFKA] Sending response - postId: {}, title: {}", response.getPostId(), response.getTitle());
            
            return response;
                    
        } catch (Exception e) {
            log.error("❌ [KAFKA] Failed to handle request - postId: {}, error: {}", 
                    request.getPostId(), e.getMessage());
            throw new RuntimeException("Failed to get post info", e);
        }
    }
    
    private String generateUrl(PostEntity post) {
        if (post.getSnsType() == SnsType.youtube) {
            return "https://youtube.com/watch?v=" + post.getSnsPostId();
        } else {
            // 다른 SNS 타입에 대한 URL 생성 로직
            return cloudFrontSigner.getThumbUrl(post.getObjectKey());
        }
    }
}
