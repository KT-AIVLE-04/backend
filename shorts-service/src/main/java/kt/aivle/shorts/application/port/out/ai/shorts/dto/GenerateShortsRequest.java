package kt.aivle.shorts.application.port.out.ai.shorts.dto;


import java.util.List;

public record GenerateShortsRequest(
        String sessionId,
        String title,
        String content,
        Integer adDuration,
        List<String> imageUrls
) {
}