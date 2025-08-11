package kt.aivle.shorts.adapter.in.web.dto.response;

import java.util.List;

public record SceneResponse(String sessionId, List<SceneItem> scenes, List<String> scenesImageList,
                            List<String> aiScenesImageList) {

    public record SceneItem(String title, String content) {
    }
}
