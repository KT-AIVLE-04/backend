package kt.aivle.shorts.application.port.out.ai.shorts.dto;

import java.util.List;

public record GenerateSceneResponse(String sessionId, List<SceneItem> scenes, List<String> scenesImageList,
                                    List<String> aiScenesImageList) {

    public record SceneItem(String title, String content) {
    }
}
