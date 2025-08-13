package kt.aivle.shorts.application.port.out.ai.shorts.dto;

import java.util.List;

public record GenerateScenarioRequest(
        String storeName,
        String businessType,
        String platform,
        String adType,
        String target,
        String prompt,
        List<String> brandConcepts
) {
}