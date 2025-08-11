package kt.aivle.shorts.application.port.out.ai.shorts.dto;


public record GenerateSceneRequest(
        String sessionId,
        String title,
        String content,
        Integer adDuration
) {
}

