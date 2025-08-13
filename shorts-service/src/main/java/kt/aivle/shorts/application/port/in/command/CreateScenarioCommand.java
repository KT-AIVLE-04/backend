package kt.aivle.shorts.application.port.in.command;


import java.util.List;

public record CreateScenarioCommand(
        Long userId,
        Long storeId,
        String prompt,
        String platform,
        String target,
        String adType,
        List<String> brandConcepts
) {
}

