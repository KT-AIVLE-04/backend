package kt.aivle.shorts.adapter.out.ai.shorts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateAiSceneResponse(
        @JsonProperty("session_id") String sessionId,
        List<SceneItem> scenes,
        @JsonProperty("scenes_image_list") List<String> scenesImageList,
        @JsonProperty("ai_scenes_image_list") List<String> aiScenesImageList) {

    public record SceneItem(String title, String content) {
    }
}
