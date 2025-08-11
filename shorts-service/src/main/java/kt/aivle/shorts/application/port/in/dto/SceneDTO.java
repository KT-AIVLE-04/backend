package kt.aivle.shorts.application.port.in.dto;

import java.util.List;

public record SceneDTO(String sessionId, List<SceneItem> scenes, List<String> scenesImageList,
                       List<String> aiScenesImageList) {

    public record SceneItem(String title, String content) {
    }
}
