package kt.aivle.shortsmvc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShortsRequest(
    @NotBlank String sessionId,
    @NotBlank String title,
    @NotBlank String content,
    @NotNull Integer adDuration
) {}