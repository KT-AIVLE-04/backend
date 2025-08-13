package kt.aivle.analytics.application.port.in.command;

import java.util.List;

import kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent.SocialPost;
import kt.aivle.analytics.domain.model.SnsType;
import lombok.Builder;

@Builder
public record CollectMetricsCommand(
    String userId,
    SnsType snsType,
    List<SocialPost> socialPosts
) {}
